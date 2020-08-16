package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.server.GameCreated
import eu.lubsen.rummikub.idl.server.GameRemoved
import eu.lubsen.rummikub.idl.server.PlayerDisconnected
import eu.lubsen.rummikub.idl.server.ServerMessage
import eu.lubsen.rummikub.model.Game
import eu.lubsen.rummikub.model.Lounge
import eu.lubsen.rummikub.model.Player
import eu.lubsen.rummikub.util.Failure
import eu.lubsen.rummikub.util.Result
import eu.lubsen.rummikub.util.Success
import java.util.UUID

fun playerIsOwner(lounge : Lounge, gameName : String, playerId : UUID) : Boolean {
    return isValidGameName(lounge = lounge, gameName = gameName)
            && isValidPlayerId(lounge = lounge, playerId = playerId)
            && lounge.games[gameName]?.owner == lounge.players[playerId]
}

fun isValidPlayerId(lounge: Lounge, playerId: UUID) : Boolean {
    return lounge.players.containsKey(key = playerId)
}

fun isValidGameName(lounge: Lounge, gameName: String) : Boolean {
    return lounge.games.containsKey(key = gameName)
}

fun playerConnects(lounge: Lounge, player: Player) {
    // assume there will be no UUID clashes
    lounge.players[player.id] = player
}

// TODO: how to handle a player dropping out of an ongoing game?
fun playerDisconnects(lounge: Lounge, player: Player) : Result<ServerMessage> {
    lounge.players.remove(key = player.id)

    lounge.games.values.forEach { it.players.remove(key = player.id) }

    val message : ServerMessage = PlayerDisconnected(eventNumber = 0, player = player)
    return Success(message)
}

fun createGame(lounge : Lounge, gameName : String, ownerId : UUID) : Result<ServerMessage> {
    return if (!lounge.games.containsKey(key = gameName)) {
        val game = Game(gameName, lounge.players[ownerId]!!)
        lounge.games[gameName] = game
        Success(
            GameCreated(eventNumber = 0, game = game)
                .addRecipient(recipients = lounge.players.keys)
        )
    } else {
        Failure(reason = "Cannot create game with '$gameName', name already exists.")
    }
}

fun removeGame(lounge : Lounge, gameName : String) : Result<ServerMessage> {
    return if (isValidGameName(lounge = lounge, gameName = gameName)) {
        val game = lounge.games.remove(key = gameName)!!
        Success(
            GameRemoved(eventNumber = 0, game = game)
                .addRecipient(recipients = lounge.players.keys)
        )
    }
    else {
        Failure(reason = "Cannot remove game '$gameName', name does not exist.")
    }
}

// TODO test
fun findGameForPlayer(lounge: Lounge, player: Player) : Result<Game> {
    val games = lounge.games.filter { (name,game) -> game.players.containsKey(player.id) }
    return if (games.isNotEmpty())
        Success(value = games.values.first())
    else
        Failure(reason = "Player has not joined any game.")
}
