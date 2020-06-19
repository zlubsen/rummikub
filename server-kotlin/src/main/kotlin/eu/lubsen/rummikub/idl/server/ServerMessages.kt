package eu.lubsen.rummikub.idl.server

import eu.lubsen.rummikub.model.Game
import eu.lubsen.rummikub.model.Player
import java.util.UUID

enum class ServerMessageType {
    Connected,
    PlayerConnected,
    PlayerDisconnected,
    GameCreated,
    GameRemoved,
    PlayerJoinedGame,
    PlayerLeftGame,
    GameStarted,
    GameStopped,
    GameFinished,
    PlayedMove,
    TilesHandToTable,
    TilesTableToHand,
    ArrangedTiles,
    TurnEnded,
    MessageResponse,
    GameListResponse,
    PlayerListResponse
}

sealed class ServerMessage constructor(val eventNumber : Long) {
    abstract val type : ServerMessageType
    var recipients : MutableList<UUID> = mutableListOf()

    abstract fun toJson() : String

    fun addRecipient(recipient : UUID) : ServerMessage {
        this.recipients.add(recipient)
        return this
    }

    fun addRecipient(recipients : Collection<UUID>) : ServerMessage {
        this.recipients.addAll(recipients)
        return this
    }
}

class Connected constructor(eventNumber: Long, val player: Player) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.Connected

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "player" : ${player.toJson()}
            }
        """.trimIndent()
    }
}

class PlayerConnected constructor(eventNumber : Long, val player : Player) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayerConnected

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "player" : ${player.toJson()}
            }
        """.trimIndent()
    }
}

class PlayerDisconnected constructor(eventNumber : Long, val player : Player) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayerDisconnected

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "player" : ${player.toJson()}
            }
        """.trimIndent()
    }
}

class GameCreated constructor(eventNumber : Long, val game : Game) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.GameCreated

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "game" : {
                    "name" : "${game.name}",
                    "gameState" : "${game.gameState}",
                    "players" : [
                        ${game.players.keys.joinToString(
                                separator = ","
                            ) { id -> "\"${id}\"" }}
                    ]
                }
            }
        """.trimIndent()
    }
}

class GameRemoved constructor(eventNumber : Long, val game: Game) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.GameRemoved

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "gameName" : "${game.name}"
            }
        """.trimIndent()
    }
}

class PlayerJoinedGame constructor(eventNumber : Long, val game: Game, val player: Player) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayerJoinedGame

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "gameName" : "${game.name}",
                "playerId" : "${player.id}"
            }
        """.trimIndent()
    }
}

class PlayerLeftGame constructor(eventNumber : Long, val game: Game, val player: Player) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayerLeftGame

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "gameName" : "${game.name}",
                "playerId" : "${player.id}"
            }
        """.trimIndent()
    }
}

class GameStarted constructor(eventNumber : Long, val game: Game) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.GameStarted

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "gameName" : "${game.name}"
            }
        """.trimIndent()
    }
}

class GameStopped constructor(eventNumber : Long, val game: Game) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.GameStopped

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "gameName" : "${game.name}"
            }
        """.trimIndent()
    }
}

class GameFinished constructor(eventNumber : Long, val game: Game) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.GameFinished

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "gameName" : "${game.name}"
            }
        """.trimIndent()
    }
}

class PlayedMove constructor(eventNumber : Long) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayedMove

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
            }
        """.trimIndent()
    }
}

//TilesHandToTable
//TilesTableToHand
//ArrangedTiles
//TurnEnded

class MessageResponse constructor(eventNumber : Long, private val message : String) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.MessageResponse

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "message" : "$message"
            }
        """.trimIndent()
    }
}

class GameListResponse constructor(eventNumber : Long, private val games : List<Game>) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.GameListResponse

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "games" : 
                    ${games.joinToString(
                        separator = ",",
                        prefix = "[",
                        postfix = "]")
                        { game -> """{"gameName" : "${game.name}"}""" }
                    }
            }
        """.trimIndent()
    }
}

class PlayerListResponse constructor(eventNumber : Long, private val players : List<Player>) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayerListResponse

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "players" : 
                    ${players.joinToString(
                        separator = ",",
                        prefix = "[",
                        postfix = "]")
                        { player -> """
                            {
                                "id" : "${player.id}",
                                "name" : "${player.playerName}"
                            }"""
                        }
                }
            }
        """.trimIndent()
    }
}