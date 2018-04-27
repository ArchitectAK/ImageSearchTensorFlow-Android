package com.cogitator.imagesearchtensorflowandroid.view.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cogitator.imagesearchtensorflowandroid.R
import com.cogitator.imagesearchtensorflowandroid.model.Product


/**
 * @author Ankit Kumar (ankitdroiddeveloper@gmail.com) on 27/04/2018 (MM/DD/YYYY)
 */
class ProductAdapter(private val products: MutableList<Product>, private val secondResult: Boolean) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes: Int = if (secondResult)
            R.layout.product_item_list
        else R.layout.second_product_item_list

        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(products[position].product_url)
                .apply(RequestOptions()
                        .centerCrop())
                .into(holder.ivProductImage)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
    }
}