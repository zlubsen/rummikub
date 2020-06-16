package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.server.GameRemoved
import eu.lubsen.rummikub.model.Game
import eu.lubsen.rummikub.model.Lounge
import java.util.UUID

fun playerIsOwner(lounge : Lounge, gameName : String, playerId : UUID) : Boolean {
    return isValidGameName(lounge = lounge, gameName = gameName)
            && isValidPlayerId(lounge = lounge, playerId = playerId)
            && lounge.games[gameName]?.owner == lounge.players[playerId]
}

fun isValidPlayerId(lounge: Lounge, playerId: UUID) : Boolean {
    return lounge.players.containsKey(playerId)
}

fun isValidGameName(lounge: Lounge, gameName: String) : Boolean {
    return lounge.games.containsKey(gameName)
}

fun createGame(lounge : Lounge, gameName : String, ownerId : UUID) : Result {
    return if (!lounge.games.containsKey(gameName)) {
        lounge.games[gameName] = Game(gameName, lounge.players[ownerId]!!)
        Success(gameName)
    } else {
        Failure("Cannot create game with '$gameName', name already exists.")
    }
}

fun removeGame(lounge : Lounge, gameName : String) : Result {
    return if (isValidGameName(lounge = lounge, gameName = gameName)) {
        val game = lounge.games.remove(gameName)!!
        game.players.forEach { (k, p) -> TODO("Server message: Notify players in the game that the game is closed") }
        Success(Pair(listOf(game.players.values), GameRemoved(0, game)))
    }
    else {
        Failure("Cannot remove game '$gameName', name does not exist.")
    }
}

