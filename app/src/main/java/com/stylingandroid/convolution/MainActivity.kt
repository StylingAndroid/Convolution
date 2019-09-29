package com.stylingandroid.convolution

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.stylingandroid.convolution.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var imageProcessor: ImageProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageProcessor = ImageProcessor(this, this, binding.processed)

        setupFilterSelector()
        setupImageSelector()
    }

    private fun setupImageSelector() {
        val images = ImageArrayFactory(this, "images").items

        binding.imageSelector.apply {
            adapter = ArrayAdapter<String>(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                images.map { it.name }
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    parent?.setSelection(0)
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    updateImage(images[position].bitmap)
                }
            }
        }
        updateImage(images[0].bitmap)
    }

    private fun updateImage(bitmap: Bitmap) {
        binding.unprocessed.setImageBitmap(bitmap)
        imageProcessor.apply {
            image = bitmap.copy(bitmap.config, bitmap.isMutable)
            process()
        }
    }

    private fun setupFilterSelector() {
        val filters = FilterArrayFactory(this, "filters").items

        binding.filterSelector.apply {
            adapter = ArrayAdapter<String>(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                filters.map { it.name }
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    parent?.setSelection(0)
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    binding.processed.setBackgroundColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            filters[position].backgroundColourId
                        )
                    )
                    updateFilter(filters[position])
                }
            }
        }
        imageProcessor.coefficients = filters[0].coefficients
    }

    private fun updateFilter(newFilter: Filter) {
        imageProcessor.apply {
            coefficients = newFilter.coefficients
        }
    }
}
