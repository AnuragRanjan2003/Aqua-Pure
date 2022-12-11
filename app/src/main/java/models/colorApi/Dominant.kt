package models.colorApi

data class Dominant(
    val b: Int,
    val g: Int,
    val hex: String,
    val hsv: List<Double>,
    val r: Int
)