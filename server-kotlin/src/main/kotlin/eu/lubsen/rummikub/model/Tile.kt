package eu.lubsen.rummikub.model

data class Tile (var value: TileNumber, var color : TileColor, val type : TileType) {
//
//    fun setJokerValue(_value: TileNumber, _color: TileColor) {
//        if (type == TileType.JOKER) {
//            value = _value
//            color = _color
//        }
//    }
//
//    fun clearJokerValue() {
//        if (type == TileType.JOKER) {
//            value = null
//            color = null
//        }
//    }
    override fun equals(other: Any?) : Boolean {
        return other is Tile
                && this.type == other.type
                && this.value == other.value
                && this.color == other.color
    }
}

fun createJoker() : Tile {
    return Tile(TileNumber.ANY, TileColor.ANY, TileType.JOKER)
}