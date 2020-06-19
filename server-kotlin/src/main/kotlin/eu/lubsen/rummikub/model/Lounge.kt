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
}