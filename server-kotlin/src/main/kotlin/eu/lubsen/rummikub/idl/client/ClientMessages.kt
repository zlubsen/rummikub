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
    PlayerMove
}

sealed class ClientMessage(json: JsonObject) {
    abstract val type : ClientMessageType
    val playerId : UUID = UUID.fromString(json.getString("playerId"))
}

class CreateGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString("gameId")
    override val type: ClientMessageType = ClientMessageType.CreateGame
}

class RemoveGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString("gameId")
    override val type: ClientMessageType = ClientMessageType.RemoveGame
}

class JoinGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString("gameId")
    override val type: ClientMessageType = ClientMessageType.JoinGame
}

class LeaveGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString("gameId")
    override val type: ClientMessageType = ClientMessageType.LeaveGame
}

class RequestGameList(json: JsonObject) : ClientMessage(json = json) {
    override val type: ClientMessageType = ClientMessageType.RequestGameList
}

class RequestPlayerList(json: JsonObject) : ClientMessage(json = json) {
    override val type: ClientMessageType = ClientMessageType.RequestPlayerList
}

class StartGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString("gameId")
    override val type: ClientMessageType = ClientMessageType.StartGame
}

class StopGame(json: JsonObject) : ClientMessage(json = json) {
    val gameName : String = json.getString("gameId")
    override val type: ClientMessageType = ClientMessageType.StopGame
}

class PlayerMove(json: JsonObject, lounge : Lounge) : ClientMessage(json = json) {
    val gameName : String = json.getString("gameId")
    val move : Move = parseMove(lounge, this, json)
    override val type: ClientMessageType = ClientMessageType.PlayerMove
}