package eu.lubsen.rummikub.core

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

fun createGame(lounge : Lounge, name : String, ownerId : UUID) : Boolean {
    return if (!lounge.games.containsKey(name)) {
        lounge.games[name] = Game(name, lounge.players[ownerId]!!)
        true
    } else {
        TODO("error message, there is already a game with 'name'")
        false
    }
}

fun removeGame(lounge : Lounge, gameName : String, ownerId : UUID) : Boolean {
    return if (playerIsOwner(lounge = lounge, gameName = gameName, playerId = ownerId)) {
        val game = lounge.games.remove(gameName)!!
        game.players.forEach { (k, p) -> TODO("Server message: Notify players in the game that the game is closed") }
        true
    }
    else {
        TODO("error message that games with 'name' does not exist")
        false
    }
}

