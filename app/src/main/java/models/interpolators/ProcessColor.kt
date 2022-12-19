package models.interpolators

import models.colorApi.Dominant

class ProcessColor(color: Dominant) {
    private val color: Dominant

    init {
        this.color = color
    }

    fun computeGreen(): Float {
        val x = ((color.r + color.b) / 2).toFloat()
        val del = color.g - x
        if (del < 0) return -1f
        return if (x == 0.00f) del
        else (del / x)
    }

    fun computeBrown(): Float {
        val x = ((color.g + color.b) / 2).toFloat()
        val del = color.r - x
        if (del < 0) return -1f
        return if (x == 0.00f) del
        else (del / x)
    }
}