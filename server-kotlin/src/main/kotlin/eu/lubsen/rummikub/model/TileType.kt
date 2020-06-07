package eu.lubsen.rummikub.model

enum class TileType {
    REGULAR,
    JOKER
}

fun tileTypeFromString(value : String) : TileType {
    return when(value.first()) {
        'J' -> TileType.JOKER
        else -> TileType.REGULAR
    }
}