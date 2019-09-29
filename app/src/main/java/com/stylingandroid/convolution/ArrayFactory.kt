package com.stylingandroid.convolution

import android.content.Context
import android.content.res.TypedArray
import android.graphics.BitmapFactory

sealed class ArrayFactory<T>(
    context: Context,
    name: String,
    itemFactory: (TypedArray, Int) -> T
) {

    private val mutableItems = mutableListOf<T>()

    init {
        context.resources.obtainTypedArray(context.getIdentifier(name, "array")).run {
            (0 until length()).forEach { index ->
                mutableItems += itemFactory(this, index)
            }
            recycle()
        }
    }

    val items: List<T>
        get() = mutableItems.toList()
}

class ImageArrayFactory(
    context: Context,
    name: String
) : ArrayFactory<Image>(context, name, { array, itemId ->
    array.getString(itemId)?.let { stringName ->
        Image(
            context.getString(context.getIdentifier(stringName, "string")),
            BitmapFactory.decodeResource(
                context.resources,
                context.getIdentifier(stringName, "drawable")
            )
        )
    } ?: throw IllegalArgumentException("String not found")
})

class FilterArrayFactory(
    context: Context,
    name: String
) : ArrayFactory<Filter>(context, name, { array, itemId ->
    array.getString(itemId)?.let { stringName ->
        Filter(
            context.getString(context.getIdentifier(stringName, "string")),
            FloatArrayFactory(context, stringName).items.toFloatArray(),
            context.getIdentifier(stringName, "color")
        )
    } ?: throw IllegalArgumentException("String not found")
})

class FloatArrayFactory(
    context: Context,
    name: String
) : ArrayFactory<Float>(context, name, { array, itemId ->
    array.getFloat(itemId, 0f)
})

private fun Context.getIdentifier(name: String, type: String) =
    resources.getIdentifier(name, type, packageName)
