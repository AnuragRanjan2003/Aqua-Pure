package models

data class Report(
    val uid: String?,
    val drinkable: Float?,
    val algae:Float?,
    val dirty:Float?,
    val date: String?,
    val lat: Double?,
    val lon: Double?
)