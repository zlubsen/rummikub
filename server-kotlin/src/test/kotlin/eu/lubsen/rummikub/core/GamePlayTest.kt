package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.model.Game
import eu.lubsen.rummikub.model.GameState
import eu.lubsen.rummikub.model.Player
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class GamePlayTest {

    private fun createPlayers(count : Int) : List<Player> {
        var players = mutableListOf<Player>()
        for (i in 1..count)
            players.add(Player(playerName = "tester$i"))
        return players
    }

    @Test
    fun twoPlayerGame() {
        val players = createPlayers(count = 2)
        var game = Game("testGame", players[0])

        assertEquals(expected = GameState.JOINING, actual = game.gameState)

        game.addPlayer(players[0])
        game.addPlayer(players[1])

        assertEquals(expected = 2, actual = game.players.size)

        game.startGame()

        assertEquals(expected = GameState.STARTED, actual = game.gameState)
    }
}