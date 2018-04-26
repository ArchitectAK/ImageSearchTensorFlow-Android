package com.cogitator.imagesearchtensorflowandroid.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.cogitator.imagesearchtensorflowandroid.R


/**
 * @author Ankit Kumar (ankitdroiddeveloper@gmail.com) on 25/04/2018 (MM/DD/YYYY)
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.setTitle(title)

        val productListFragment = ProductListFragment()
        productListFragment.setTopResult("all")

        supportFragmentManager.beginTransaction().add(R.id.activity_main, productListFragment).commit()

    }
}