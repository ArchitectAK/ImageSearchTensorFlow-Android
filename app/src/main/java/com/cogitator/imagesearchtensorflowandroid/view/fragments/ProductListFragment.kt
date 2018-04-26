package com.cogitator.imagesearchtensorflowandroid.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.cogitator.imagesearchtensorflowandroid.R
import com.cogitator.imagesearchtensorflowandroid.network.APIClient
import com.cogitator.imagesearchtensorflowandroid.network.RetrofitInterface
import kotlinx.android.synthetic.main.fragment_product_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


/**
 * @author Ankit Kumar (ankitdroiddeveloper@gmail.com) on 26/04/2018 (MM/DD/YYYY)
 */
class ProductListFragment : Fragment() {
    var topLayoutManager: RecyclerView.LayoutManager? = null
    var secondLayoutManager: RecyclerView.LayoutManager? = null
    var rvTopProducts: RecyclerView? = null
    var rvSecondProducts: RecyclerView? = null
    var topResult: String? = null
    var secondResult: String? = null
    var mSimilarItems: Boolean = false
    var fabButtonOpenCamera: FloatingActionButton? = null
    private var retrofitInterface: RetrofitInterface? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_product_list, container, false)

        return rootView
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mSimilarItems) {
            tvProductCategory.text = "View similar products"
            tvSecondCategory.text = "You can also view"
            //  if(!secondResult.equalsIgnoreCase("all"))

        }

        btnDetectObject.visibility = View.VISIBLE

        btnDetectObject.setOnClickListener {
            val cameraFragment = CameraFragment()
            activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.activity_main, cameraFragment)
                    .commit()

            btnDetectObject.visibility = View.GONE
        }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        topLayoutManager = GridLayoutManager(activity, 2)
        secondLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        rvTopProducts.setLayoutManager(topLayoutManager)
        rvSecondProducts.setLayoutManager(secondLayoutManager)
    }



    private fun loadProductImage(topResultArg: String) {

        retrofitInterface = APIClient().getClient().create(RetrofitInterface::class.java)

        val call = retrofitInterface?.getProductList()
        call.enqueue(object : Callback<Products>() {
            fun onResponse(call: Call<Products>, response: Response<Products>) {
                val products = response.body()
                var customProducts: ArrayList<Product>
                customProducts = ArrayList()
                for (i in 0 until products.getProducts().size()) {
                    if (topResultArg.equals("all", ignoreCase = true)) {
                        customProducts = products.getProducts()
                        break
                    } else if (products.getProducts().get(i).getProductLabel().equalsIgnoreCase(topResultArg)) {
                        customProducts.add(products.getProducts().get(i))
                    }
                }
                if (topResultArg.equals("none", ignoreCase = true)) {
                    Toast.makeText(context, "No similar items available!", Toast.LENGTH_SHORT).show()
                    rvTopProducts.setAdapter(ProductAdapter(products.getProducts(), false))
                } else {
                    rvTopProducts.setAdapter(ProductAdapter(customProducts, false))
                }
            }

            fun onFailure(call: Call<Products>, t: Throwable) {
                Log.d("Error", t.message)
            }
        })
    }


}