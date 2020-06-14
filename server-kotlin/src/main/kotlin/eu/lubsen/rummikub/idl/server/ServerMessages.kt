package eu.lubsen.rummikub.idl.server

import eu.lubsen.rummikub.model.Game
import eu.lubsen.rummikub.model.Player

enum class ServerMessageType {
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
    InvalidMove,
    GameListResponse,
    PlayerListResponse
}

sealed class ServerMessage constructor(val eventNumber : Long) {
    abstract val type : ServerMessageType

    abstract fun toJson() : String
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
                    "name" : ${game.name},
                    "gameState" : ${game.gameState},
                    "players" : [
                        ${game.players.keys.joinToString { "," }}
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
                "gameName" : ${game.name}
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
                "gameName" : ${game.name},
                "playerId" : ${player.id}
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
                "gameName" : ${game.name},
                "playerId" : ${player.id}
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
                "gameName" : ${game.name}
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
                "gameName" : ${game.name}
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
                "gameName" : ${game.name}
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

class InvalidMove constructor(eventNumber : Long, private val message : String) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.InvalidMove

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "error" : "$message"
            }
        """.trimIndent()
    }
}

class GameListResponse constructor(eventNumber : Long, val games : List<Game>) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.GameListResponse

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "games" : [
                    ${games.joinToString { """ "gameName" : "${it.name}", """ }}
                ]
            }
        """.trimIndent()
    }
}

class PlayerListResponse constructor(eventNumber : Long, val players : List<Player>) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayerListResponse

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "players" : [
                    ${players.joinToString { """
                        [
                            "id" : "${it.id}",
                            "name" : "${it.playerName}"
                        ] """.trimIndent()
                    }}
                ]
            }
        """.trimIndent()
    }
}