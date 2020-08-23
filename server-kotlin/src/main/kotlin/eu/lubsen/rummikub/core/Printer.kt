package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.model.Tile
import eu.lubsen.rummikub.model.TileType

fun prettyPrintTile(tile: Tile) : String {
    return when(tile.type) {
        TileType.JOKER -> "J"
        TileType.REGULAR -> {
            var color = tile.color.toString().toLowerCase().capitalize()
            var number = tile.number.ordinal.toString()
            return "$color-$number"
        }
    }
}