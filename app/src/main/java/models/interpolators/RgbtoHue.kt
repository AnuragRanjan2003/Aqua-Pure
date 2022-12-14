package models.interpolators

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class RgbtoHue(red:Int ,green:Int ,blue:Int) {
    private val r:Int
    private val g:Int
    private val b:Int
    init {
        r= red
        g=green
        b=blue
    }
     fun getHue():Double{
        val max = max(max(r,g),b)
        val min = min(min(r,g),b)

        if(max==min) return 0.00
        var hue=0.00
        if(max==r){
            hue = 0.00+((g-b)/(max-min))
        }
        else if(max==g){
            hue = 2.00 + (b-r)/(max-min)
        }else{
            hue = 4.00 + (r-g)/(max-min)
        }
        hue *= 60
        if(hue<0) hue += 360
        return round(hue)
    }
}