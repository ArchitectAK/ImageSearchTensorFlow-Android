package com.cogitator.imagesearchtensorflowandroid.classifier

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Trace
import android.util.Log
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.io.*
import java.util.*


/**
 * @author Ankit Kumar (ankitdroiddeveloper@gmail.com) on 25/04/2018 (MM/DD/YYYY)
 */
class TensorFlowImageClassifier : Classifier {

    val TAG = "TAG"
    // Only return this many results with at least this confidence.
    private val MAX_RESULTS = 3
    private val THRESHOLD = 0.1f

    private var inferenceInterface: TensorFlowInferenceInterface? = null

    // Config values.
    private var inputName: String? = null
    private var outputName: String? = null
    private var inputSize: Int = 0
    private var imageMean: Int = 0
    private var imageStd: Float = 0.toFloat()

    // Pre-allocated buffers.
    private var labels = Vector<String>()
    private var intValues: IntArray? = null
    private var floatValues: FloatArray? = null
    private var outputs: FloatArray? = null
    private var outputNames: Array<String>? = null

    private val logStats = false

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param inputSize The input size. A square image of inputSize x inputSize is assumed.
     * @param imageMean The assumed mean of the image values.
     * @param imageStd The assumed std of the image values.
     * @param inputName The label of the image input node.
     * @param outputName The label of the output node.
     * @throws IOException
     */
    fun create(
            assetManager: AssetManager,
            modelFilename: String,
            labelFilename: String,
            inputSize: Int,
            imageMean: Int,
            imageStd: Float,
            inputName: String,
            outputName: String): Classifier {
        val c = TensorFlowImageClassifier()
        c.inputName = inputName
        c.outputName = outputName

        // Read the label names into memory.
        val actualFilename = labelFilename.split("file:///android_asset/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        Log.i(TAG, "Reading labels from: $actualFilename")

        val inputStream: InputStream = assetManager.open(actualFilename)

        inputStream.bufferedReader().useLines { lines -> lines.forEach { c.labels.add(it) } }
        c.labels.forEach { println(">  $it") }


//        File(actualFilename).readLines().forEach{
//            c.labels.add(it)
//        }
//        var br: BufferedReader? = null
//        try {
//            br = BufferedReader(InputStreamReader(assetManager.open(actualFilename)))
//            var line: String
//            line = br.readLine()
//            while (line.isNotEmpty()) {
//                c.labels.add(line)
//                if (br.readLine() != null)
//                    line = br.readLine()
//            }
//            br.close()
//        } catch (e: IOException) {
//            throw RuntimeException("Problem reading label file!", e)
//        }

        c.inferenceInterface = TensorFlowInferenceInterface(assetManager, modelFilename)

        // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
        val operation = c.inferenceInterface?.graphOperation(outputName)
        val numClasses = operation?.output<Int>(0)?.shape()?.size(1)
        Log.i(TAG, "Read " + c.labels.size + " labels, output layer size is " + numClasses)

        // Ideally, inputSize could have been retrieved from the shape of the input operation.  Alas,
        // the placeholder node for input in the graphdef typically used does not specify a shape, so it
        // must be passed in as a parameter.
        c.inputSize = inputSize
        c.imageMean = imageMean
        c.imageStd = imageStd

        // Pre-allocate buffers.
        c.outputNames = arrayOf(outputName)
        c.intValues = IntArray(inputSize * inputSize)
        c.floatValues = FloatArray(inputSize * inputSize * 3)
        c.outputs = FloatArray(numClasses?.toInt()!!)

        return c
    }

    override fun recognizeImage(bitmap: Bitmap): List<Classifier.Recognition> {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage")

        Trace.beginSection("preprocessBitmap")
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (i in intValues?.indices!!) {
            val value = intValues!![i]
            floatValues?.set(i * 3 + 0, ((value shr 16 and 0xFF) - imageMean) / imageStd)
            floatValues?.set(i * 3 + 1, ((value shr 8 and 0xFF) - imageMean) / imageStd)
            floatValues?.set(i * 3 + 2, ((value and 0xFF) - imageMean) / imageStd)
        }
        Trace.endSection()

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed")
        inferenceInterface?.feed(inputName, floatValues, 1, inputSize as Long, inputSize as Long, 3L)
        Trace.endSection()

        // Run the inference call.
        Trace.beginSection("run")
        inferenceInterface?.run(outputNames, logStats)
        Trace.endSection()

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch")
        inferenceInterface?.fetch(outputName, outputs)
        Trace.endSection()

        // Find the best classifications.
        val pq = PriorityQueue(
                3,
                Comparator<Classifier.Recognition> { lhs, rhs ->
                    // Intentionally reversed to put high confidence at the head of the queue.
                    java.lang.Float.compare(rhs.confidence!!, lhs.confidence!!)
                })
        for (i in outputs?.indices!!) {
            if (outputs!![i] > THRESHOLD) {
                pq.add(
                        Classifier.Recognition(
                                "" + i, if (labels.size > i) labels[i] else "unknown", outputs!![i]))
            }
        }
        val recognitions = ArrayList<Classifier.Recognition>()
        val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        Trace.endSection() // "recognizeImage"
        return recognitions
    }

    override fun close() {
        inferenceInterface?.close()
    }


}