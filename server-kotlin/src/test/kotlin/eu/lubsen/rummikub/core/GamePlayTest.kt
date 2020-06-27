package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.server.PlayedTookFromHeap
import eu.lubsen.rummikub.idl.server.ServerMessage
import eu.lubsen.rummikub.idl.server.ServerMessageType
import eu.lubsen.rummikub.model.*
import eu.lubsen.rummikub.util.Failure
import eu.lubsen.rummikub.util.Success
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        val game = Game("mergeTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        player1.initialPlay = true
        game.addPlayer(player2)
        player2.initialPlay = true
        startGameWith(game = game,
            tableTiles = listOf(parseTileSet("Bla1-Bla2-Bla3"), parseTileSet("Blu1-Blu2-Blu3")),
            playerOneHand = listOf(parseTileSet("Red1-Red2-Red3"), parseTileSet("Red4-Red5-Red6")),
            playerTwoHand = listOf(parseTileSet("Yel1-Red1-Blu1"), parseTileSet("Bla5")),
            heap = listOf(Tile(type = TileType.REGULAR, color = TileColor.RED, number = TileNumber.TEN)))

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
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("mergeTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        player1.initialPlay = true
        player1.hasPlayedInTurn = true
        game.addPlayer(player2)
        player2.initialPlay = true
        startGameWith(game = game,
            tableTiles = listOf(parseTileSet("Bla1-Bla2-Bla3"), parseTileSet("Blu1-Blu2-Blu3")),
            playerOneHand = listOf(parseTileSet("Red1"), parseTileSet("Red4")),
            playerTwoHand = listOf(parseTileSet("Yel1"), parseTileSet("Bla5")),
            heap = listOf(Tile(type = TileType.REGULAR, color = TileColor.RED, number = TileNumber.TEN)))

        game.turn.tilesIntroduced.add(Tile(TileNumber.TEN, TileColor.BLACK, TileType.REGULAR))

        val move = Move(game, player1, MoveType.END_TURN)
        val result = handlePlayerMove(lounge = lounge, gameName = game.name, move = move)

        assertTrue(result.isSuccess())
        assertEquals(player2, game.getCurrentPlayer())
        assertFalse(playerHasPlayedInTurn(game, player2))
        assertTrue(tableIsValid(game = game))
        val success = result as Success<ServerMessage>
        val message = success.result()
        assertEquals(ServerMessageType.PlayedTurnEnded, message.type)
    }

    @Test
    fun playerEndsTurnInvalidTable() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("invalidEndTurnTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        player1.initialPlay = true
        player1.hasPlayedInTurn = true
        game.addPlayer(player2)
        player2.initialPlay = true
        startGameWith(game = game,
            tableTiles = listOf(parseTileSet("Bla1-Bla2-Bla3"), parseTileSet("Blu1-Blu2-Blu3"), parseTileSet("Red1")),
            playerOneHand = listOf(parseTileSet("Red4")),
            playerTwoHand = listOf(parseTileSet("Yel1"), parseTileSet("Bla5")),
            heap = listOf(Tile(type = TileType.REGULAR, color = TileColor.RED, number = TileNumber.TEN)))

        val move = Move(game, player1, MoveType.END_TURN)
        val result = handlePlayerMove(lounge = lounge, gameName = game.name, move = move)

        assertTrue(result.isFailure())
        assertEquals(player1, game.getCurrentPlayer())
        assertFalse(tableIsValid(game = game))
        val failure = result as Failure<ServerMessage>
        val message = failure.message()
        assertTrue(message.isNotBlank())
    }

    @Test
    fun playerTakesFromHeap() {
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
            playerOneHand = listOf(parseTileSet("Red1-Red2"), parseTileSet("Red5-Red6")),
            playerTwoHand = listOf(parseTileSet("Yel1-Red1-Blu1"), parseTileSet("Bla5")),
            heap = listOf(Tile(type = TileType.REGULAR, color = TileColor.RED, number = TileNumber.TEN)))

        val move = Move(game, player1, MoveType.TAKE_FROM_HEAP)
        val result = handlePlayerMove(lounge = lounge, gameName = game.name, move = move)

        assertTrue(result.isSuccess())
        assertEquals(player2, game.getCurrentPlayer())
        val success = result as Success<ServerMessage>
        val message = success.result()
        assertEquals(ServerMessageType.PlayedTookFromHeap, message.type)
        assertTrue(message is PlayedTookFromHeap)
        assertEquals(3, player1.hand.size)
    }

    @Test
    fun playerTakesFromHeapResetTable() {
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
            playerOneHand = listOf(parseTileSet("Red1-Red2"), parseTileSet("Red5-Red6")),
            playerTwoHand = listOf(parseTileSet("Yel1-Red1-Blu1"), parseTileSet("Bla5")),
            heap = listOf(Tile(type = TileType.REGULAR, color = TileColor.RED, number = TileNumber.TEN)))

        val playMove = Move(game, player1, MoveType.HAND_TO_TABLE)
        playMove.tilesToRelocate = player1.hand.values.first().id
        handlePlayerMove(lounge = lounge, gameName = game.name, move = playMove)

        assertEquals(3, game.table.size)
        assertEquals(1, player1.hand.size)

        val endMove = Move(game, player1, MoveType.TAKE_FROM_HEAP)
        val result = handlePlayerMove(lounge = lounge, gameName = game.name, move = endMove)

        assertTrue(result.isSuccess())
        assertEquals(player2, game.getCurrentPlayer())
        assertEquals(2, game.table.size)
        assertEquals(3, player1.hand.size)

        val success = result as Success<ServerMessage>
        val message = success.result()
        assertEquals(ServerMessageType.PlayedTookFromHeap, message.type)
        assertTrue(message is PlayedTookFromHeap)
    }

    @Test
    fun playerPutsTilesOnTable() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("handToTableTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        player1.initialPlay = true
        game.addPlayer(player2)
        player2.initialPlay = true
        startGameWith(game = game,
            tableTiles = listOf(parseTileSet("Bla1-Bla2-Bla3"), parseTileSet("Blu1-Blu2-Blu3")),
            playerOneHand = listOf(parseTileSet("Red1"), parseTileSet("Red4")),
            playerTwoHand = listOf(parseTileSet("Yel1"), parseTileSet("Bla5")),
            heap = listOf(Tile(type = TileType.REGULAR, color = TileColor.RED, number = TileNumber.TEN)))

        assertFalse(playerHasPlayedInTurn(game, player1))

        val move = Move(game, player1, MoveType.HAND_TO_TABLE)
        move.tilesToRelocate = player1.hand.values.first().id
        val result = handlePlayerMove(lounge = lounge, gameName = game.name, move = move)

        assertTrue(result.isSuccess())
        assertEquals(player1, game.getCurrentPlayer())
        assertTrue(playerHasPlayedInTurn(game, player1))
        assertFalse(tableIsValid(game = game))
        val success = result as Success<ServerMessage>
        val message = success.result()
        assertEquals(ServerMessageType.PlayedTilesHandToTable, message.type)
        assertEquals(3, game.table.size)
    }

    @Test
    fun playerPutsTilesOnTableAndBack() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("handToTableTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        player1.initialPlay = true
        game.addPlayer(player2)
        player2.initialPlay = true
        startGameWith(game = game,
            tableTiles = listOf(parseTileSet("Bla1-Bla2-Bla3"), parseTileSet("Blu1-Blu2-Blu3")),
            playerOneHand = listOf(parseTileSet("Red1"), parseTileSet("Red4")),
            playerTwoHand = listOf(parseTileSet("Yel1"), parseTileSet("Bla5")),
            heap = listOf(Tile(type = TileType.REGULAR, color = TileColor.RED, number = TileNumber.TEN)))

        assertFalse(playerHasPlayedInTurn(game, player1))

        val toTableMove = Move(game, player1, MoveType.HAND_TO_TABLE)
        val movedTilesId = player1.hand.values.first().id
        toTableMove.tilesToRelocate = movedTilesId
        handlePlayerMove(lounge = lounge, gameName = game.name, move = toTableMove)

        assertTrue(playerHasPlayedInTurn(game, player1))
        assertFalse(tableIsValid(game = game))
        assertEquals(3, game.table.size)

        val backToHandMove = Move(game, player1, MoveType.TABLE_TO_HAND)
        backToHandMove.tilesToRelocate = movedTilesId
        val result = handlePlayerMove(lounge = lounge, gameName = game.name, move = backToHandMove)

        assertTrue(result.isSuccess())
        assertTrue(tableIsValid(game = game))
        assertFalse(playerHasPlayedInTurn(game, player1))

        val success = result as Success<ServerMessage>
        val message = success.result()
        assertEquals(ServerMessageType.PlayedTilesTableToHand, message.type)
        assertEquals(2, game.table.size)
    }

    @Test
    fun playerPutsTilesInHandNotPlayed() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("handToTableTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        player1.initialPlay = true
        game.addPlayer(player2)
        player2.initialPlay = true
        startGameWith(game = game,
            tableTiles = listOf(parseTileSet("Blu5")),
            playerOneHand = listOf(parseTileSet("Red1"), parseTileSet("Red4")),
            playerTwoHand = listOf(parseTileSet("Yel1"), parseTileSet("Bla5")),
            heap = listOf(Tile(type = TileType.REGULAR, color = TileColor.RED, number = TileNumber.TEN)))

        assertFalse(playerHasPlayedInTurn(game, player1))

        val backToHandMove = Move(game, player1, MoveType.TABLE_TO_HAND)
        backToHandMove.tilesToRelocate = game.table.values.first().id
        val result = handlePlayerMove(lounge = lounge, gameName = game.name, move = backToHandMove)

        assertTrue(result.isFailure())
        assertFalse(playerHasPlayedInTurn(game, player1))

        val failure = result as Failure<ServerMessage>
        val message = failure.message()
        println(message)
        assertTrue(message.isNotBlank())
        assertEquals(1, game.table.size)
    }

    @Test
    fun playerPlaysInitialPlay() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("InitialPlayTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        game.addPlayer(player2)

        startGameWith(game = game,
            tableTiles = listOf(),
            playerOneHand = listOf(parseTileSet("Red10-Blu10-Yel10"), parseTileSet("Bla1-Bla2")),
            playerTwoHand = listOf(parseTileSet("Yel1"), parseTileSet("Bla5")),
            heap = listOf())

        val initialMove = Move(game, player1, MoveType.HAND_TO_TABLE)
        initialMove.tilesToRelocate = player1.hand.values.first().id
        val result = handlePlayerMove(lounge, game.name, initialMove)

        assertTrue(result.isSuccess())
        assertEquals(player1, game.getCurrentPlayer())
        assertEquals(30, tileListValue(game.turn.tilesIntroduced))
    }

    @Test
    fun playerInitiallyCannotArrangeTable() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("InitialPlayTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        game.addPlayer(player2)

        startGameWith(game = game,
            tableTiles = listOf(parseTileSet("Yel6-Yel7-Yel8-Yel9")),
            playerOneHand = listOf(parseTileSet("Red10-Blu10-Yel10"), parseTileSet("Bla1-Bla2")),
            playerTwoHand = listOf(parseTileSet("Yel1")),
            heap = listOf())

        val arrangeMove = Move(game, player1, MoveType.SPLIT)
        arrangeMove.setSplit(location = MoveLocation.TABLE, tileSetId = game.table.values.first().id, index = 2)

        val result = handlePlayerMove(lounge, game.name, arrangeMove)

        assertTrue(result.isFailure())
        val failure = result as Failure<ServerMessage>
        val message = failure.message()
        assertTrue(message.isNotBlank())
    }
}