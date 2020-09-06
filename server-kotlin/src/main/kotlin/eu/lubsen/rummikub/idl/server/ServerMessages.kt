package eu.lubsen.rummikub.idl.server

import eu.lubsen.rummikub.core.isValidTileSet
import eu.lubsen.rummikub.model.*
import java.util.UUID

enum class ServerMessageType {
    Connected,
    PlayerConnected,
    PlayerDisconnected,
    PlayerNameExists,
    GameCreated,
    GameRemoved,
    PlayerJoinedGame,
    PlayerLeftGame,
    GameStarted,
    GameStopped,
    GameFinished,
    PlayedTilesHandToTable,
    PlayedTilesTableToHand,
    TableChangedHandToTable,
    TableChangedTableToHand,
    PlayedTurnEnded,
    PlayedTookFromHeap,
    PlayerTookFromHeap,
    PlayedTileSetSplit,
    PlayedTileSetsMerged,
    PlayedTileSetsMovedAndMerged,
    TableChangedMovedAndMerged,
    MessageResponse,
    GameListResponse,
    PlayerListResponse,
    PlayerListForGameResponse,
    GameStateResponse
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
                "player" : ${playerToJson(player)}
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
                "player" : ${playerToJson(player)}
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
                "player" : ${playerToJson(player)}
            }
        """.trimIndent()
    }
}

class PlayerNameExists constructor(eventNumber: Long, val name: String) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayerNameExists

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "error" : "A player with name '$name' already exists."
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
                "game" : ${gameToJson(game)}
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
                "gameName" : "${game.name}",
                "gameState" : "${game.gameState}"
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
                "gameName" : "${game.name}",
                "gameState" : "${game.gameState}"
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
                "gameName" : "${game.name}",
                "gameState" : "${game.gameState}",
                "winner" : "${game.getCurrentPlayer().id}"
            }
        """.trimIndent()
    }
}

class PlayedTilesHandToTable constructor(eventNumber : Long, private val move : MoveOk) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayedTilesHandToTable

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "playerId" : "${move.playerId}",
                "tileSet" : ${tileSetToJson(move.tileSet)}
            }
        """.trimIndent()
    }
}

class PlayedTilesTableToHand constructor(eventNumber : Long, private val move : MoveOk) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayedTilesTableToHand

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "playerId" : "${move.playerId}",
                "tileSet" : ${tileSetToJson(move.tileSet)}
            }
        """.trimIndent()
    }
}

class TableChangedHandToTable constructor(eventNumber : Long, private val move : MoveOk) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.TableChangedHandToTable

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "playerId" : "${move.playerId}",
                "tileSet" : ${tileSetToJson(move.tileSet)}
            }
        """.trimIndent()
    }
}

class TableChangedTableToHand constructor(eventNumber : Long, private val move : MoveOk) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.TableChangedTableToHand

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "playerId" : "${move.playerId}",
                "tileSet" : ${tileSetToJson(move.tileSet)}
            }
        """.trimIndent()
    }
}

class PlayedTookFromHeap constructor(eventNumber : Long, private val move : MoveOk) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayedTookFromHeap

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "playerId" : "${move.playerId}",
                "tileSet" : ${tileSetToJson(move.tileSet)}
            }
        """.trimIndent()
    }
}

class PlayerTookFromHeap constructor(eventNumber : Long, private val move : MoveOk) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayerTookFromHeap

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "playerId" : "${move.playerId}"
            }
        """.trimIndent()
    }
}

class PlayedTurnEnded constructor(eventNumber : Long, private val move : TurnEnded) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayedTurnEnded

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "playerId" : "${move.playerId}",
                "nextPlayerId" : "${move.nextPlayerId}"
            }
        """.trimIndent()
    }
}

class PlayedTileSetSplit constructor(eventNumber : Long, private val move : TilesSplit) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayedTileSetSplit

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "location" : "${move.location}",
                "leftSet" : ${tileSetToJson(move.leftSet)},
                "rightSet" : ${tileSetToJson(move.rightSet)},
                "originalId" : "${move.originalId}"
            }
        """.trimIndent()
    }
}

class PlayedTileSetsMerged constructor(eventNumber : Long, private val move : TilesMerged) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayedTileSetsMerged

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "location" : "${move.location}",
                "sourceId" : "${move.sourceId}",
                "targetId" : "${move.targetId}",
                "tileSet" : ${tileSetToJson(move.mergedSet)}
            }
        """.trimIndent()
    }
}

class PlayedTileSetsMovedAndMerged constructor(eventNumber : Long, private val move : TilesMovedAndMerged) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayedTileSetsMovedAndMerged

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "playerId" : "${move.playerId}",
                "sourceLocation" : "${move.sourceLocation}",
                "targetLocation" : "${move.targetLocation}",
                "sourceId" : "${move.sourceId}",
                "targetId" : "${move.targetId}",
                "tileSet" : ${tileSetToJson(move.mergedSet)}
            }
        """.trimIndent()
    }
}

class TableChangedMovedAndMerged constructor(eventNumber: Long, private val move: TilesMovedAndMerged) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.TableChangedMovedAndMerged

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "sourceLocation" : "${move.sourceLocation}",
                "targetLocation" : "${move.targetLocation}",
                "sourceId" : "${move.sourceId}",
                "targetId" : "${move.targetId}",
                "tileSet" : ${tileSetToJson(move.mergedSet)}
            }
        """.trimIndent()
    }
}

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
                        { gameToJson(it) }
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
                        { playerToJson(it) }
                }
            }
        """.trimIndent()
    }
}

class PlayerListForGameResponse constructor(eventNumber : Long, private val gameName : String, private val players : List<Player>) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.PlayerListForGameResponse

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "gameName" : "$gameName",
                "players" : 
                    ${players.joinToString(
                        separator = ",",
                        prefix = "[",
                        postfix = "]")
                        { playerToJson(it) }
                }
            }
        """.trimIndent()
    }
}

class GameStateResponse constructor(eventNumber: Long, private val game: Game, private val player: Player) : ServerMessage(eventNumber = eventNumber) {
    override val type: ServerMessageType = ServerMessageType.GameStateResponse

    override fun toJson(): String {
        return """
            {
                "messageType" : "$type",
                "eventNumber" : $eventNumber,
                "game" : ${gameToJson(game)},
                "currentPlayer" : "${game.getCurrentPlayer().id}",
                "gameState" : "${game.gameState}",
                "table" : ${game.table.values.joinToString(
                    separator = ",",
                    prefix = "[",
                    postfix = "]")
                    { tileSetToJson(it) }
                },
                "hand" : ${player.hand.values.joinToString(
                    separator = ",",
                    prefix = "[",
                    postfix = "]")
                    { tileSetToJson(it) }
                },
                "players" : ${game.players.values.joinToString(
                    separator = ",",
                    prefix = "[",
                    postfix = "]")
                    { playerToJson(it) }
                } 
            }
        """.trimIndent()
    }
}

private fun tileSetToJson(tileSet: TileSet) : String {
    return """
        {
            "id" : "${tileSet.id}",
            "isValid" : ${isValidTileSet(tileSet)},
            "tiles" : ${tileSet.tiles.joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]")
            { tileToJson(it) }}
        }
    """.trimIndent()
}

private fun tileToJson(tile: Tile) : String {
    return """
        {
            "color" : "${tile.color.name}",
            "number" : "${tile.number.ordinal}",
            "isJoker" : ${tile.type == TileType.JOKER}
        }
    """.trimIndent()
}

private fun playerToJson(player: Player) : String {
    return """
        {
            "id" : "${player.id}",
            "name" : "${player.playerName}"
        }
    """.trimIndent()
}

private fun gameToJson(game: Game) : String {
    return """
        {
            "gameName" : "${game.name}",
            "gameState" : "${game.gameState}",
            "owner" : "${game.owner.id}",
            "players" : [
                ${game.players.keys.joinToString(
                    separator = ","
                ) { id -> "\"${id}\"" }}
            ]
        }
    """.trimIndent()
}