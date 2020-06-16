package eu.lubsen.rummikub.model

import eu.lubsen.rummikub.core.Failure
import eu.lubsen.rummikub.core.Result
import eu.lubsen.rummikub.core.Success
import java.util.UUID

class Lounge {
    var players = mutableMapOf<UUID, Player>()
    var games = mutableMapOf<String, Game>()

    fun playerConnects(player: Player) {
        players[player.id] = player
    }

    fun playerDisconnects(player: Player) {
        players.remove(player.id)

        games.values.forEach { it.players.remove(player.id) }
        // TODO: how to handle a player dropping out of an ongoing game?
    }

    fun listPlayers() : List<Player> {
        return players.values.toList()
    }

    fun listGames() : List<Game> {
        return games.values.toList()
    }

    fun createGame(name : String, owner : Player) : Result {
        return if (!games.contains(name)) {
            val game = Game(name, owner)
            games[game.name] = game
            Success(game)
        } else {
            Failure("There is already a game with name '$name'")
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