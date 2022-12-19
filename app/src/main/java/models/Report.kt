package models

data class Report(
    val uid: String?="",
    val drinkable: Float?=0f,
    val algae:Float?=0f,
    val dirty:Float?=0f,
    val date: String?="",
    val lat: Double?=0.00,
    val lon: Double?=0.00
)