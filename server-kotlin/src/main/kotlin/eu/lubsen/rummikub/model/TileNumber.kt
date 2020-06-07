package eu.lubsen.rummikub.model

enum class TileNumber {
    ANY,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    ELEVEN,
    TWELVE,
    THIRTEEN;
}

fun tileNumberFromString(value: String) : TileNumber {
    return when(value) {
        "1" -> TileNumber.ONE
        "2" -> TileNumber.TWO
        "3" -> TileNumber.THREE
        "4" -> TileNumber.FOUR
        "5" -> TileNumber.FIVE
        "6" -> TileNumber.SIX
        "7" -> TileNumber.SEVEN
        "8" -> TileNumber.EIGHT
        "9" -> TileNumber.NINE
        "10" -> TileNumber.TEN
        "11" -> TileNumber.ELEVEN
        "12" -> TileNumber.TWELVE
        "13" -> TileNumber.THIRTEEN
        else -> TileNumber.ANY
    }
}