package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.model.*

fun parseTileSet(string: String) : TileSet {
    val pieces = string.split("-")
    var tiles = mutableListOf<Tile>()

    for (piece in pieces) {
        when(piece.length) {
            1 -> tiles.add(createJoker())
            else -> {
                val color = piece.substring(0, 3)
                val number = piece.substring(3)
                val tile = Tile(tileNumberFromString(number), tileColorFromString(color), tileTypeFromString(piece))
                tiles.add(tile)
            }
        }
    }
    return TileSet(tiles)
}