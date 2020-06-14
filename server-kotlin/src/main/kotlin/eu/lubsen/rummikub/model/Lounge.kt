package eu.lubsen.rummikub.model

import java.util.UUID

class Lounge {
    var players = mutableMapOf<UUID, Player>()
    var games = mutableMapOf<String, Game>()

    fun listPlayers() : List<Player> {
        return players.values.toList()
    }

    fun listGames() : List<Game> {
        return games.values.toList()
    }

    fun createGame(name : String, owner : Player) : Boolean {
        return if (!games.contains(name)) {
            val game = Game(name, owner)
            games[game.name] = game
            true
        } else {
            false
        }
    }

    fun removeGame(gameName : String) : Boolean {
        return if (games.containsKey(gameName)) {
            games.remove(gameName)
            true
        } else
            false
    }

    fun joinGame(playerId: UUID, gameName : String) : Boolean {
        return if (games.containsKey(gameName) && players.containsKey(playerId)) {
            players[playerId]?.let { games[gameName]?.addPlayer(it) }
            true
        } else {
            false
        }
    }

    fun leaveGame(playerId: UUID, gameName : String) : Boolean {
        return if (games.containsKey(gameName) && players.containsKey(playerId)) {
            players[playerId]?.let { games[gameName]?.removePlayer(it) }
            true
        } else {
            false
        }
    }
}