package com.stylingandroid.convolution

import androidx.annotation.ColorRes

data class Filter(
    val name: String,
    val coefficients: FloatArray,
    @ColorRes val backgroundColourId: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Filter

        if (name != other.name) return false
        if (!coefficients.contentEquals(other.coefficients)) return false
        if (backgroundColourId != other.backgroundColourId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + coefficients.contentHashCode()
        result = 31 * result + backgroundColourId
        return result
    }
}
