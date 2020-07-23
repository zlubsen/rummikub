package eu.lubsen.rummikub.model

import java.util.UUID

class TileSet constructor(val tiles : List<Tile>){
    val id: UUID = UUID.randomUUID()

    override fun equals(other : Any?) : Boolean {
        return other is TileSet
                && this.tiles == other.tiles
    }
}