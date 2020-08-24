package eu.lubsen.rummikub.idl.client

import eu.lubsen.rummikub.core.parseMove
import eu.lubsen.rummikub.model.Lounge
import eu.lubsen.rummikub.model.Move
import io.vertx.core.json.JsonObject
import java.util.*

enum class ClientMessageType {
    CreateGame,
    RemoveGame,
    JoinGame,
    LeaveGame,
    StartGame,
    StopGame,
    RequestGameList,
    RequestPlayerList,
    RequestPlayerListForGame,
    RequestGameState,
    PlayerMove
}

sealed class ClientMessage(json: JsonObject) {
    abstract val type : ClientMessageType
    val playerId : UUID = UUID.fromString(json.getString(PLAYER_ID_FIELD))
}

class CreateGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString(GAME_ID_FIELD)
    override val type: ClientMessageType = ClientMessageType.CreateGame
}

class RemoveGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString(GAME_ID_FIELD)
    override val type: ClientMessageType = ClientMessageType.RemoveGame
}

class JoinGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString(GAME_ID_FIELD)
    override val type: ClientMessageType = ClientMessageType.JoinGame
}

class LeaveGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString(GAME_ID_FIELD)
    override val type: ClientMessageType = ClientMessageType.LeaveGame
}

class RequestGameList(json: JsonObject) : ClientMessage(json = json) {
    override val type: ClientMessageType = ClientMessageType.RequestGameList
}

class RequestPlayerList(json: JsonObject) : ClientMessage(json = json) {
    override val type: ClientMessageType = ClientMessageType.RequestPlayerList
}

class RequestPlayerListForGame(json: JsonObject) : ClientMessage(json = json) {
    override val type: ClientMessageType = ClientMessageType.RequestPlayerListForGame
    val gameName : String = json.getString(GAME_ID_FIELD)
}

class RequestGameState(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString(GAME_ID_FIELD);
    override val type: ClientMessageType = ClientMessageType.RequestGameState
}

class StartGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString(GAME_ID_FIELD)
    override val type: ClientMessageType = ClientMessageType.StartGame
}

class StopGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString(GAME_ID_FIELD)
    override val type: ClientMessageType = ClientMessageType.StopGame
}

class PlayerMove(json: JsonObject, lounge : Lounge) : ClientMessage(json = json) {
    val gameName : String = json.getString(GAME_ID_FIELD)
    val move : Move = parseMove(lounge, this, json)
    override val type: ClientMessageType = ClientMessageType.PlayerMove
}

const val GAME_ID_FIELD = "gameId"
const val PLAYER_ID_FIELD = "playerId"
const val MOVE_TYPE_FIELD = "moveType"
const val TILESET_ID_FIELD = "tileSetId"
const val SPLIT_SET_ID = "splitSetId"
const val SOURCE_ID_FIELD = "sourceId"
const val TARGET_ID_FIELD = "targetId"
const val SOURCE_LOCATION_FIELD = "sourceLocation"
const val TARGET_LOCATION_FIELD = "targetLocation"
const val MOVE_LOCATION_FIELD = "moveLocation"
const val INDEX_FIELD = "index"
