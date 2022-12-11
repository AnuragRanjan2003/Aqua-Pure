package models.colorApi

data class Request(
    val id: String,
    val operations: Int,
    val timestamp: Double
)