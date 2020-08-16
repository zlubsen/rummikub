package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.client.PlayerMove
import eu.lubsen.rummikub.model.*
import io.vertx.core.json.JsonObject
import java.util.*

fun parseTileSet(string: String) : TileSet {
    val pieces = string.split("-")
    val tiles = mutableListOf<Tile>()

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

fun parseMove(lounge: Lounge, message : PlayerMove, json: JsonObject) : Move {
    val game : Game = lounge.games[message.gameName]!!
    val player : Player = lounge.players[message.playerId]!!
    val moveType : MoveType = MoveType.valueOf(json.getString("moveType"))
    val move = Move(game, player, moveType)

    when (move.moveType) {
        MoveType.HAND_TO_TABLE -> {
            move.tilesToRelocate = UUID.fromString(json.getString("tileSetId"))
        }
        MoveType.TABLE_TO_HAND -> {
            move.tilesToRelocate = UUID.fromString(json.getString("tileSetId"))
        }
        MoveType.SPLIT -> {
            move.setSplit(
                location = MoveLocation.valueOf(json.getString("moveLocation")),
                tileSetId = UUID.fromString(json.getString("splitSetId")),
                index = json.getInteger("splitIndex")
            )
        }
        MoveType.MERGE -> {
            move.setMerger(
                location = MoveLocation.valueOf(json.getString("moveLocation")),
                sourceId = UUID.fromString(json.getString("sourceId")),
                targetId = UUID.fromString(json.getString("targetId")),
                index = json.getInteger("index")
            )
        }
        MoveType.TAKE_FROM_HEAP -> {}
        MoveType.END_TURN -> {}
    }
    return move
}