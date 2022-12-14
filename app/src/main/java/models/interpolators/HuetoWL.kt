package models.interpolators

class HuetoWL(h: Double) {
    private val hue: Double

    init {
        hue = h
    }

    fun computeWl(): Double {
        val z = (hue - 120) / 69.71

        return 1.798 * pow(z, 10) - 3.073 * pow(z, 9) - 15.74 * pow(z, 8) + 21.36 * pow(
            z,
            7
        ) + 50.25 * pow(z, 6) - 57.18 * pow(z, 5) - 72.7 * pow(z, 4) + 72.15 * pow(
            z,
            3
        ) + 49.87 * pow(z, 2) - 75.9 * z + 518.9
    }

    private fun pow(a: Double, b: Int): Double {
        if (b == 0) return 1.00
        if (b == 1) return a

        return if (b % 2 == 0) pow(a * a, b / 2)
        else a * pow(a * a, (b - 1) / 2)
    }


}