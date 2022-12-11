package models.colorApi

data class Colors(
    val accent: List<Accent>,
    val dominant: Dominant,
    val other: List<Other>
)