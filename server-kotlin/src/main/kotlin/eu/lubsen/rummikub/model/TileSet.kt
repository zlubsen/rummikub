package eu.lubsen.rummikub.model

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.UUID

class TileSet constructor(val tiles : List<Tile>){
    val id: UUID = UUID.randomUUID()

    fun toJson() : JsonObject {
        return JsonObject()
            .put("id", id.toString())
            .put("tiles", JsonArray(tiles))
    }

    override fun equals(other : Any?) : Boolean {
        return other is TileSet
                && this.tiles == other.tiles
    }
}