package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.client.*
import eu.lubsen.rummikub.model.*
import io.vertx.core.json.JsonObject
import java.util.UUID

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
    val moveType : MoveType = MoveType.valueOf(json.getString(MOVE_TYPE_FIELD))
    val move = Move(game, player, moveType)

    when (move.moveType) {
        MoveType.HAND_TO_TABLE -> {
            move.setMove(
                sourceLoc = MoveLocation.HAND,
                targetLoc = MoveLocation.TABLE,
                tileSetId = UUID.fromString(json.getString(TILESET_ID_FIELD)))
        }
        MoveType.TABLE_TO_HAND -> {
            move.setMove(sourceLoc = MoveLocation.TABLE,
                targetLoc = MoveLocation.HAND,
                tileSetId = UUID.fromString(json.getString(TILESET_ID_FIELD)))
        }
        MoveType.MOVE_AND_MERGE -> {
            move.setMove(sourceLoc = MoveLocation.valueOf(json.getString(SOURCE_LOCATION_FIELD)),
                targetLoc = MoveLocation.valueOf(json.getString(TARGET_LOCATION_FIELD)),
                tileSetId = UUID.fromString(json.getString(SOURCE_ID_FIELD)))
            move.setMerger(
                location = MoveLocation.valueOf(json.getString(TARGET_LOCATION_FIELD)),
                sourceId = UUID.fromString(json.getString(SOURCE_ID_FIELD)),
                targetId = UUID.fromString(json.getString(TARGET_ID_FIELD)),
                index = json.getInteger(INDEX_FIELD)
            )
        }
        MoveType.SPLIT -> {
            move.setSplit(
                location = MoveLocation.valueOf(json.getString(MOVE_LOCATION_FIELD)),
                tileSetId = UUID.fromString(json.getString(SPLIT_SET_ID)),
                index = json.getInteger(INDEX_FIELD)
            )
        }
        MoveType.MERGE -> {
            move.setMerger(
                location = MoveLocation.valueOf(json.getString(TARGET_LOCATION_FIELD)),
                sourceId = UUID.fromString(json.getString(SOURCE_ID_FIELD)),
                targetId = UUID.fromString(json.getString(TARGET_ID_FIELD)),
                index = json.getInteger(INDEX_FIELD)
            )
        }
        MoveType.TAKE_FROM_HEAP -> {}
        MoveType.END_TURN -> {}
        MoveType.RESET_TURN -> {}
    }
    return move
}