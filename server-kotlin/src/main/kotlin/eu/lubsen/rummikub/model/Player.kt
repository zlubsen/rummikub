package eu.lubsen.rummikub.model

import java.util.UUID

class Player constructor(val playerName : String) {
    var id: UUID = UUID.randomUUID()
    var hand : MutableMap<UUID, TileSet> = mutableMapOf()
    var initialPlay : Boolean = false

    fun tileToHand(tile : Tile) : TileSet {
        val tileGroup = TileSet(listOf(tile))
        hand[tileGroup.id] = tileGroup
        return tileGroup
    }
}