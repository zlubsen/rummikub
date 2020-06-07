package eu.lubsen.rummikub.model

enum class TileColor {
    ANY,
    BLUE,
    RED,
    BLACK,
    YELLOW
}

fun tileColorFromString(value: String) : TileColor {
    return when(value) {
        "Blu" -> TileColor.BLUE
        "Red" -> TileColor.RED
        "Bla" -> TileColor.BLACK
        "Yel" -> TileColor.YELLOW
        else -> TileColor.ANY
    }
}