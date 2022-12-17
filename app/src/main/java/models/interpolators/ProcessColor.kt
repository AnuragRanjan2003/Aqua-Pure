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
        var r =0.00f
        var g_r =0.00f
        val x = ((color.g + color.b) / 2).toFloat()
        val del = color.r - x
        if (del < 0) r= -1f
        r = if (x == 0.00f) del
        else (del / x)

         val del2 = (color.g - color.b).toFloat()
        if(del2<0) g_r= -1f
        g_r = if (color.b==0) del2
        else (del2/color.b)

        return r-g_r
    }
}