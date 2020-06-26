package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.server.ServerMessage
import eu.lubsen.rummikub.idl.server.ServerMessageType
import eu.lubsen.rummikub.model.*
import eu.lubsen.rummikub.util.Failure
import eu.lubsen.rummikub.util.Success
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GamePlayTest {

    private fun createPlayers(count : Int) : List<Player> {
        val players = mutableListOf<Player>()
        for (i in 1..count)
            players.add(Player(playerName = "tester$i"))
        return players
    }

    @Test
    fun twoPlayerGame() {
        val players = createPlayers(count = 2)
        val game = Game("testGame", players[0])

        assertEquals(expected = GameState.JOINING, actual = game.gameState)

        game.addPlayer(players[0])
        game.addPlayer(players[1])

        assertEquals(expected = 2, actual = game.players.size)

        game.startGame()

        assertEquals(expected = GameState.STARTED, actual = game.gameState)
    }

    @Test
    fun playerMergesTilesInHandInTurn() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("drawTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        player1.initialPlay = true
        game.addPlayer(player2)
        player2.initialPlay = true
        startGameWith(game = game,
            tableTiles = listOf(parseTileSet("Bla1-Bla2-Bla3"), parseTileSet("Blu1-Blu2-Blu3")),
            playerOneHand = listOf(parseTileSet("Red1-Red2-Red3"), parseTileSet("Red4-Red5-Red6")),
            playerTwoHand = listOf(parseTileSet("Yel1-Red1-Blu1"), parseTileSet("Bla5")))

        val move = Move(game, player1, MoveType.MERGE)
        move.moveLocation = MoveLocation.HAND
        move.setMerger(MoveLocation.HAND, player1.hand.values.elementAt(0).id, player1.hand.values.elementAt(1).id)
        val result = handlePlayerMove(lounge = lounge, gameName = game.name, move = move)

        assertTrue(result.isSuccess())
        val success = result as Success<ServerMessage>
        val message = success.result()
        assertEquals(ServerMessageType.PlayedTileSetsMerged, message.type)
        assertEquals(
            "[RED-ONE, RED-TWO, RED-THREE, RED-FOUR, RED-FIVE, RED-SIX]",
            JsonObject(message.toJson()).getJsonObject("tileSet").getJsonArray("tiles").list.toString()
        )
    }

    @Test
    fun playerEndsTurnValidTable() {
        TODO()
    }

    @Test
    fun playerEndsTurnInvalidTable() {
        TODO()
    }

    @Test
    fun playerTakesFromHeap() {
        TODO()
    }
}