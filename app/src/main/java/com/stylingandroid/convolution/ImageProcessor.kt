package com.stylingandroid.convolution

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicConvolve3x3
import android.renderscript.ScriptIntrinsicConvolve5x5
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageProcessor(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    private val imageView: ImageView
) : CoroutineScope, LifecycleObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val renderScript: RenderScript = RenderScript.create(context)
    var image: Bitmap? = null
        set(value) {
            field = value
            imageUpdated()
        }
    var coefficients: FloatArray? = null
        set(value) {
            field = value
            if (::inputAllocation.isInitialized) {
                process()
            }
        }

    private lateinit var inputAllocation: Allocation
    private lateinit var outputAllocation: Allocation
    private lateinit var script3x3: ScriptIntrinsicConvolve3x3
    private lateinit var script5x5: ScriptIntrinsicConvolve5x5

    private fun imageUpdated() {
        destroyAllocations()
        inputAllocation = Allocation.createFromBitmap(renderScript, image).apply {
            copyFrom(image)
        }
        outputAllocation = Allocation.createFromBitmap(renderScript, image)
        script3x3 = ScriptIntrinsicConvolve3x3.create(renderScript, inputAllocation.element)
        script5x5 = ScriptIntrinsicConvolve5x5.create(renderScript, inputAllocation.element)
    }

    private fun destroyAllocations() {
        if (::inputAllocation.isInitialized) {
            inputAllocation.destroy()
        }
        if (::outputAllocation.isInitialized) {
            outputAllocation.destroy()
        }
        if (::script3x3.isInitialized) {
            script3x3.destroy()
        }
        if (::script5x5.isInitialized) {
            script5x5.destroy()
        }
    }

    fun process() {
        launch {
            imageView.setImageBitmap(withContext(Dispatchers.Default) {
                processImage()
            })
        }
    }

    private fun processImage(): Bitmap? {
        return image?.let { input ->
            val output = Bitmap.createBitmap(input)
            when (coefficients?.size ?: 0) {
                MATRIX_3X3 -> script3x3.apply {
                    setInput(inputAllocation)
                    setCoefficients(coefficients)
                    forEach(outputAllocation)
                }
                MATRIX_5X5 -> script5x5.apply {
                    setInput(inputAllocation)
                    setCoefficients(coefficients)
                    forEach(outputAllocation)
                }
                else ->
                    @Suppress("UseCheckOrError")
                    throw IllegalStateException("Invalid coefficients")
            }
            outputAllocation.copyTo(output)
            output
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Suppress("Unused")
    fun onDestroy() {
        job.cancel()
        destroyAllocations()
        renderScript.destroy()
    }

    private companion object {
        private const val MATRIX_3X3 = 9
        private const val MATRIX_5X5 = 25
    }
}
