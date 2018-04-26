package com.cogitator.imagesearchtensorflowandroid.network

import com.cogitator.imagesearchtensorflowandroid.model.Products
import retrofit2.Call
import retrofit2.http.GET



/**
 * @author Ankit Kumar (ankitdroiddeveloper@gmail.com) on 26/04/2018 (MM/DD/YYYY)
 */
interface RetrofitInterface {
    @GET("raw/0eb67ff4bfa2da3de2d5ee7ea7533e12240863f6/product.json")
    fun getProductList(): Call<Products>
}