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
import com.cogitator.imagesearchtensorflowandroid.model.Product
import com.cogitator.imagesearchtensorflowandroid.model.Products
import com.cogitator.imagesearchtensorflowandroid.network.APIClient
import com.cogitator.imagesearchtensorflowandroid.network.RetrofitInterface
import com.cogitator.imagesearchtensorflowandroid.view.adapters.ProductAdapterr
import kotlinx.android.synthetic.main.fragment_product_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


/**
 * @author Ankit Kumar (ankitdroiddeveloper@gmail.com) on 26/04/2018 (MM/DD/YYYY)
 */
class ProductListFragment : Fragment() {
    private var topLayoutManager: RecyclerView.LayoutManager? = null
    private var secondLayoutManager: RecyclerView.LayoutManager? = null
    private var topResult: String? = null
    private var secondResult: String? = null
    private var mSimilarItems: Boolean = false
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
        rvProducts.layoutManager = topLayoutManager
        rvSecondProducts?.layoutManager = secondLayoutManager
    }


    private fun loadProductImage(topResultArg: String) {

        retrofitInterface = APIClient().getClient().create(RetrofitInterface::class.java)

        val call = retrofitInterface?.getProductList()
        call?.enqueue(object : Callback<Products> {
            override fun onFailure(call: Call<Products>?, t: Throwable?) {
            }

            override fun onResponse(call: Call<Products>?, response: Response<Products>?) {
                val products = response?.body()
                var customProducts: MutableList<Product>
                customProducts = ArrayList()
                for (i in products?.products?.indices!!) {
                    if (topResultArg.equals("all", true)) {
                        customProducts = products.products as MutableList<Product>
                        break
                    } else if (products.products[i].product_label.equals(topResultArg, true)) {
                        customProducts.add(products.products[i])
                    }
                }
                if (topResultArg.equals("none", true)) {
                    Toast.makeText(context, "No similar items available!", Toast.LENGTH_SHORT).show()
                    rvProducts.adapter = ProductAdapterr(products.products as MutableList<Product>, false)
                } else {
                    rvProducts.adapter = ProductAdapterr(customProducts, false)
                }

            }
        })

    }

    private fun loadSecondResultsImage(secondResultArg: String) {

        retrofitInterface = APIClient().getClient().create(RetrofitInterface::class.java)

        val call = retrofitInterface?.getProductList()
        call?.enqueue(object : Callback<Products> {
            override fun onResponse(call: Call<Products>, response: Response<Products>) {
                val products = response.body()
                val customProducts: MutableList<Product> = ArrayList()
                for (i in products?.products?.indices!!) {
                    if (products.products[i].product_label.equals(secondResultArg, true)) {
                        customProducts.add(products.products[i])
                    }
                }
                if (!secondResultArg.equals("all", ignoreCase = true))
                    rvSecondProducts.adapter = ProductAdapterr(customProducts, true)
                else
                    rvSecondProducts.adapter = ProductAdapterr(products.products as MutableList<Product>, true)

            }

            override fun onFailure(call: Call<Products>, t: Throwable) {
                Log.d("Error", t.message)
            }
        })
    }


    fun setTopResult(result: String) {
        topResult = result
        loadProductImage(topResult!!)
    }

    fun setSecondResult(result: String) {
        secondResult = result
        loadSecondResultsImage(secondResult!!)
    }

    fun setSimilarItems(similarItems: Boolean) {
        mSimilarItems = similarItems
    }

}