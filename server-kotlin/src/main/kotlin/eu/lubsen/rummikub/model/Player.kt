package eu.lubsen.rummikub.model

import io.vertx.core.json.JsonObject
import java.util.UUID

class Player constructor(val playerName : String) {
    var id: UUID = UUID.randomUUID()
    var hand : MutableMap<UUID, TileSet> = mutableMapOf()
    var initialPlay : Boolean = false

    init {
        initialPlay = false
    }

    fun tileToHand(tile : Tile) : TileSet {
        val tileGroup = TileSet(listOf(tile))
        hand[tileGroup.id] = tileGroup
        return tileGroup
    }

    fun toJson() : JsonObject {
        return JsonObject()
            .put("id", id.toString())
            .put("hand", hand.values.toList().map { tileGroup -> tileGroup.toJson() })
    }
}