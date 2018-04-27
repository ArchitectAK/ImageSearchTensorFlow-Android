package com.cogitator.imagesearchtensorflowandroid.view.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cogitator.imagesearchtensorflowandroid.R
import com.wonderkiln.camerakit.*
import kotlinx.android.synthetic.main.fragment_camera.*
import com.cogitator.imagesearchtensorflowandroid.classifier.TensorFlowImageClassifier
import com.cogitator.imagesearchtensorflowandroid.classifier.Classifier
import android.support.design.widget.FloatingActionButton
import com.wonderkiln.camerakit.CameraView
import java.util.concurrent.Executors


/**
 * @author Ankit Kumar (ankitdroiddeveloper@gmail.com) on 27/04/2018 (MM/DD/YYYY)
 */
class CameraFragment : Fragment() {

    private var topResult: String? = null
    private var secondResult = "all"
    private var topResultConfidence: Float? = null
    private var secondResultConfidence: Float? = null

    private val cameraView: CameraView? = null
    private val fabCamera: FloatingActionButton? = null

    private val INPUT_SIZE = 224
    private val IMAGE_MEAN = 128
    private val IMAGE_STD = 128.0f
    private val INPUT_NAME = "input"
    private val OUTPUT_NAME = "final_result"

    private val MODEL_FILE = "file:///android_asset/graph.pb"
    private val LABEL_FILE = "file:///android_asset/labels.txt"

    private var classifier: Classifier? = null

    private val executor = Executors.newSingleThreadExecutor()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_camera, container, false)
        cameraView?.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) {

            }

            override fun onError(cameraKitError: CameraKitError) {

            }

            override fun onImage(cameraKitImage: CameraKitImage) {

                var bitmap = cameraKitImage.bitmap

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

                val results = classifier?.recognizeImage(bitmap)

                topResult = results?.get(0)?.title
                topResultConfidence = results?.get(0)?.confidence
                Log.d("LOL first", topResult + topResultConfidence)

                val size = results?.size!! - 1
                if (size >= 1) {
                    secondResult = results[1].title.toString()
                    secondResultConfidence = results[1].confidence
                    Log.d("LOL second", secondResult + secondResultConfidence)

                    if (secondResultConfidence!! < 0.5) {
                        secondResult = "all"
                    }
                }

                if (topResultConfidence!! < 0.7) {
                    topResult = "none"
                }



                activity!!.supportFragmentManager.beginTransaction().remove(this@CameraFragment).commit()

                val productListFragment = ProductListFragment()
                topResult?.let { productListFragment.setTopResult(it) }
                productListFragment.setSecondResult(secondResult)
                if (topResult.equals("none", true)) {
                    productListFragment.setSimilarItems(false)
                } else
                    productListFragment.setSimilarItems(true)
                activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.activity_main, productListFragment, null)
                        ?.commit()


            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) {

            }
        })

        fabClick.setOnClickListener { cameraView?.captureImage() }

        initTensorFlowAndLoadModel(rootView)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        cameraView?.start()
    }

    override fun onPause() {
        cameraView?.stop()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        executor.execute({ classifier?.close() })
    }

    private fun initTensorFlowAndLoadModel(rootview: View) {
        executor.execute({
            try {

                classifier = TensorFlowImageClassifier().create(
                        rootview.context.assets,
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME)


            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        })
    }


}