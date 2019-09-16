package com.stylingandroid.convolution

import android.graphics.BitmapFactory
import android.os.Bundle
import android.renderscript.*
import androidx.appcompat.app.AppCompatActivity
import com.stylingandroid.convolution.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val filterCoefficients = floatArrayOf(-1f, -1f, -1f, -1f, 8f, -1f, -1f, -1f, -1f)

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        processImage()
    }

    private fun processImage() {
        val renderScript = RenderScript.create(this)
        val filter = ScriptIntrinsicConvolve3x3.create(renderScript, Element.F32(renderScript))
        filter.setCoefficients(filterCoefficients)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.image)
        val allocation = bitmap.let {
            Allocation.createFromBitmap(renderScript, it).apply {
                copyFrom(it)
            }
        }
        filter.setInput(allocation)
        filter.forEach(allocation)
        allocation.copyTo(bitmap)
        binding.processed.setImageBitmap(bitmap)
        renderScript.destroy()
    }
}
