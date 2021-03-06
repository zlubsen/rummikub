package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.model.*
import eu.lubsen.rummikub.util.Failure
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GameLogicTest {

    @Test
    fun gameStartTooFewPlayers() {
        val player1 = Player("tester1")
        val game = Game("tooFewPlayersTest", player1)
        game.addPlayer(player1)
        val result = startGame(game = game)
        assertTrue(result is Failure)
    }

    @Test
    fun gameStartTooManyPlayers() {
        val player1 = Player("tester1")
        val game = Game("tooManyPlayersTest", player1)
        game.addPlayer(player1)
        game.addPlayer(Player("tester2"))
        game.addPlayer(Player("tester3"))
        game.addPlayer(Player("tester4"))
        game.addPlayer(Player("tester5"))
        val result = startGame(game = game)
        assertTrue(result is Failure)
    }

    @Test
    fun playerDrawsFromHeap() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("drawTest", player1)
        game.addPlayer(player1)
        game.addPlayer(player2)
        game.startGame()
        val heapSize = game.heap.size

        assertEquals(14, player1.hand.size)
        assertEquals(14, player2.hand.size)
        assertTrue(playerDrawsFromHeap(game, player1).isSuccess())
        assertEquals(15, player1.hand.size)
        assertEquals(14, player2.hand.size)
        assertEquals(heapSize - 1, game.heap.size)
    }

    @Test
    fun playerPutsTilesOnTable() {
        val player1 = Player("tester1")
        val game = Game("handToTableTest", player1)
        game.addPlayer(player1)
        game.addPlayer(Player("tester2"))
        game.startGame()
        val tileSet = getFirstRegularTileSetFromHand(player1)

        assertNotNull(tileSet)
        assertTrue(game.table.isEmpty())
        assertTrue(player1.hand.containsKey(tileSet!!.id))

        playerPutsTilesOnTable(game, player1, tileSet.id)

        assertTrue(game.table.containsKey(tileSet.id))
        assertFalse(player1.hand.containsKey(tileSet.id))
        assertTrue(game.turn.tilesIntroduced.isNotEmpty())
        assertEquals(tileSet.tiles, game.turn.tilesIntroduced)
    }

    @Test
    fun split() {
        val tileSetOf3 = parseTileSet("Bla4-Blu4-Red4")
        val parts = split(tileSetOf3, 1)
        assertEquals(parts[0], parseTileSet("Bla4"))
        assertEquals(parts[1], parseTileSet("Blu4-Red4"))
    }

    @Test
    fun splitHead() {
        val tileSetOf3 = parseTileSet("Bla4-Blu4-Red4")
        val parts = split(tileSetOf3, 0)
        assertEquals(1, parts.size)
        assertEquals(parseTileSet("Bla4-Blu4-Red4"), parts[0])
    }

    @Test
    fun splitTail() {
        val tileSetOf3 = parseTileSet("Bla4-Blu4-Red4")
        val parts = split(tileSetOf3, 3)
        assertEquals(1, parts.size)
        assertEquals(parseTileSet("Bla4-Blu4-Red4"), parts[0])
    }

    @Test
    fun mergeAtStart() {
        val source = parseTileSet("Bla1")
        val target = parseTileSet("Bla2-Bla3")
        val index = 0
        val merged = merge(source = source, target = target, index = index)
        assertEquals(parseTileSet("Bla1-Bla2-Bla3"), merged)
    }

    @Test
    fun mergeAtIndex() {
        val source = parseTileSet("Bla2")
        val target = parseTileSet("Bla1-Bla3")
        val index = 1
        val merged = merge(source = source, target = target, index = index)
        assertEquals(parseTileSet("Bla1-Bla2-Bla3"), merged)
    }

    @Test
    fun mergeWithEmpty() {
        val source = TileSet(listOf())
        val target = parseTileSet("Yel10-Yel11")
        val index = 0
        val merged = merge(source = source, target = target, index = index)
        assertEquals(parseTileSet("Yel10-Yel11"), merged)
        assertNotEquals(source.id, merged.id)
    }

    @Test
    fun findTileGroup() {
    }

    @Test
    fun isTooShortGroup() {
        val input = "Bla4-Blu4"
        val tileSet = parseTileSet(input)
        assertFalse(isValidGroup(tileSet))
    }

    @Test
    fun isTooLongGroup() {
        val input = "Bla4-Blu4-Red4-Yel4-Bla4"
        val tileSet = parseTileSet(input)
        assertFalse(isValidGroup(tileSet))
    }

    @Test
    fun isValidGroup() {
        val tileSetOf3 = parseTileSet("Bla4-Blu4-Red4")
        assertTrue(isValidGroup(tileSetOf3))

        val tileSetOf4 = parseTileSet("Bla4-Blu4-Red4-Yel4")
        assertTrue(isValidGroup(tileSetOf4))
    }

    @Test
    fun isInvalidGroup() {
        val input = "Bla4-Bla4-Red4-Yel4"
        val tileSet = parseTileSet(input)
        assertFalse(isValidGroup(tileSet))
    }

    @Test
    fun isValidGroupWithJoker() {
        val tileSetOf3 = parseTileSet("Bla4-Blu4-J")
        assertTrue(isValidGroup(tileSetOf3))

        val tileSetOf4 = parseTileSet("J-Bla4-Blu4-Red4")
        assertTrue(isValidGroup(tileSetOf4))

        val tileSetTwoJokers = parseTileSet("J-Bla4-J")
        assertTrue(isValidGroup(tileSetTwoJokers))
    }

    @Test
    fun isTooShortRun() {
        val input = "Bla1-Bla2"
        val tileSet = parseTileSet(input)
        assertFalse(isValidRun(tileSet))
    }

    @Test
    fun isTooLongRun() {
        val input = "Bla1-Bla2-Bla3-Bla4-Bla5-Bla6-Bla7-Bla8-Bla9-Bla10-Bla11-Bla12-Bla13-Bla14"
        val tileSet = parseTileSet(input)
        assertFalse(isValidRun(tileSet))
    }

    @Test
    fun isValidRun() {
        val input = "Bla1-Bla2-Bla3-Bla4"
        val tileSet = parseTileSet(input)
        assertTrue(isValidRun(tileSet))
    }

    @Test
    fun isValidRunWithJokerAtStart() {
        val input = "J-Bla2-Bla3-Bla4"
        val tileSet = parseTileSet(input)
        assertTrue(isValidRun(tileSet))
    }

    @Test
    fun isInvalidRunWithJokerAtStart() {
        val input = "J-Bla1-Bla2"
        val tileSet = parseTileSet(input)
        assertFalse(isValidRun(tileSet))
    }

    @Test
    fun isValidRunWithJokerAtEnd() {
        val input = "Bla3-Bla4-J"
        val tileSet = parseTileSet(input)
        assertTrue(isValidRun(tileSet))
    }

    @Test
    fun isInvalidRunWithJokerAtEnd() {
        val input = "Bla12-Bla13-J"
        val tileSet = parseTileSet(input)
        assertFalse(isValidRun(tileSet))
    }

    @Test
    fun allSameTileColor() {
        val input = "Bla1-Bla2-Bla3-Bla4"
        val tileSet = parseTileSet(input)
        assertTrue(allSameTileColor(tileSet.tiles))
    }

    @Test
    fun allSameTileColorWithJoker() {
        val input = "Bla1-Bla2-J"
        val tileSet = parseTileSet(input)
        assertTrue(allSameTileColor(tileSet.tiles))
    }

    @Test
    fun notAllSameTileColor() {
        val input = "Bla1-Bla2-Bla3-Yel4"
        val tileSet = parseTileSet(input)
        assertFalse(allSameTileColor(tileSet.tiles))
    }

    @Test
    fun allSameTileNumber() {
        val input = "Bla4-Blu4-Red4-Yel4"
        val tileSet = parseTileSet(input)
        assertTrue(allSameTileNumber(tileSet.tiles))
    }

    @Test
    fun notAllSameTileNumber() {
        val input = "Bla4-Blu4-Red4-Yel5"
        val tileSet = parseTileSet(input)
        assertFalse(allSameTileNumber(tileSet.tiles))
    }

    @Test
    fun allUniqueTileColor() {
        val input = "Bla4-Blu4-Red4-Yel4"
        val tileSet = parseTileSet(input)
        assertTrue(allUniqueTileColor(tileSet.tiles))
    }

    @Test
    fun allUniqueTileColorWithJoker() {
        val input = "Bla4-Blu4-J"
        val tileSet = parseTileSet(input)
        assertTrue(allUniqueTileColor(tileSet.tiles))
    }

    @Test
    fun notAllUniqueTileColor() {
        val input = "Bla4-Bla4-Red4-Yel4"
        val tileSet = parseTileSet(input)
        assertFalse(allUniqueTileColor(tileSet.tiles))
    }

    @Test
    fun allUniqueTileNumber() {
        val input = "Bla1-Bla2-Bla3-Bla4"
        val tileSet = parseTileSet(input)
        assertTrue(allUniqueTileNumber(tileSet.tiles))
    }

    @Test
    fun allUniqueTileNumberWithJokerAtStart() {
        val input = "J-Bla4-Bla5"
        val tileSet = parseTileSet(input)
        assertTrue(allUniqueTileNumber(tileSet.tiles))
    }

    @Test
    fun allUniqueTileNumberWithJokerAtEnd() {
        val input = "Bla4-Bla5-J"
        val tileSet = parseTileSet(input)
        assertTrue(allUniqueTileNumber(tileSet.tiles))
    }

    @Test
    fun notAllUniqueTileNumber() {
        val input = "Bla4-Bla5-Bla5"
        val tileSet = parseTileSet(input)
        assertFalse(allUniqueTileColor(tileSet.tiles))
    }

    @Test
    fun allSequentialTileNumber() {
        val input = "Bla1-Bla2-Bla3-Bla4"
        val tileSet = parseTileSet(input)
        assertTrue(allSequentialTileNumber(tileSet.tiles))
    }

    @Test
    fun allSequentialTileNumberWithJoker() {
        val input = "Bla1-Bla2-J-Bla4"
        val tileSet = parseTileSet(input)
        assertTrue(allSequentialTileNumber(tileSet.tiles))
    }

    @Test
    fun allSequentialTileNumberWithJokerAtStart() {
        val input = "J-Bla2-Bla3-Bla4"
        val tileSet = parseTileSet(input)
        assertTrue(allSequentialTileNumber(tileSet.tiles))
    }

    @Test
    fun allSequentialTileNumberWithJokerAtEnd() {
        val input = "Bla1-Bla2-Bla3-Bla4-J"
        val tileSet = parseTileSet(input)
        assertTrue(allSequentialTileNumber(tileSet.tiles))
    }

    @Test
    fun notAllSequentialTileNumber() {
        val input = "Bla1-Bla2-Bla4"
        val tileSet = parseTileSet(input)
        assertFalse(allSequentialTileNumber(tileSet.tiles))
    }

    @Test
    fun tileIsJoker() {
        assertTrue(tileIsJoker(createJoker()))
    }

    @Test
    fun tileIsRegular() {
        val tile = Tile(TileNumber.ONE, TileColor.BLACK, TileType.REGULAR)
        assertTrue(tileIsRegular(tile))
    }

    @Test
    fun tileSetValueRun() {
        val input = "Bla1-Bla2-Bla3"
        val tileSet = parseTileSet(input)
        assertEquals(1 + 2 + 3, tileSetValue(tileSet))
    }

    @Test
    fun tileSetValueRunWithJokerAtEnd() {
        val input = "Bla1-Bla2-J"
        val tileSet = parseTileSet(input)
        assertEquals(1 + 2 + 3, tileSetValue(tileSet))
    }

    @Test
    fun tileSetValueRunWithJokerAtStart() {
        val input = "J-Bla4-Bla5"
        val tileSet = parseTileSet(input)
        assertEquals(3 + 4 + 5, tileSetValue(tileSet))
    }

    @Test
    fun tileSetValueRunWithJokerInMiddle() {
        val input = "Bla11-J-Bla13"
        val tileSet = parseTileSet(input)
        assertEquals(11 + 12 + 13, tileSetValue(tileSet))
    }

    @Test
    fun tileSetValueGroup() {
        val input = "Bla6-Blu6-Yel6-Red6"
        val tileSet = parseTileSet(input)
        assertEquals(4 * 6, tileSetValue(tileSet))
    }

    @Test
    fun tileSetValueGroupWithJokerInMiddle() {
        val input = "Bla6-Blu6-J-Red6"
        val tileSet = parseTileSet(input)
        assertEquals(6 + 6 + 6 + 6, tileSetValue(tileSet))
    }

    @Test
    fun tileSetValueGroupWithJokerAtStart() {
        val input = "J-Bla6-Blu6-Red6"
        val tileSet = parseTileSet(input)
        assertEquals(6 + 6 + 6 + 6, tileSetValue(tileSet))
    }

    @Test
    fun tileSetValueGroupWithJokerAtEnd() {
        val input = "Bla6-Blu6-Red6-J"
        val tileSet = parseTileSet(input)
        assertEquals(6 + 6 + 6 + 6, tileSetValue(tileSet))
    }

    @Test
    fun tableIsActuallyValid() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("validTableTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        player1.initialPlay = true
        game.addPlayer(player2)
        player2.initialPlay = true
        startGameWith(game = game,
            tableTiles = listOf(parseTileSet("Bla1-Bla2-Bla3"), parseTileSet("Blu1-Blu2-Blu3")),
            playerOneHand = listOf(),
            playerTwoHand = listOf(),
            heap = listOf())

        assertTrue(tableIsValid(game = game))
    }

    @Test
    fun tableIsValidButEmpty() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("validEmptyTableTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        player1.initialPlay = true
        game.addPlayer(player2)
        player2.initialPlay = true
        startGameWith(game = game,
            tableTiles = listOf(),
            playerOneHand = listOf(),
            playerTwoHand = listOf(),
            heap = listOf())

        assertTrue(tableIsValid(game = game))
    }

    @Test
    fun tableIsNotValid() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("invalidTableTest", player1)
        val lounge = Lounge()
        lounge.games[game.name] = game
        game.addPlayer(player1)
        player1.initialPlay = true
        game.addPlayer(player2)
        player2.initialPlay = true
        startGameWith(game = game,
            tableTiles = listOf(parseTileSet("Bla1-Blu2-Red3")),
            playerOneHand = listOf(),
            playerTwoHand = listOf(),
            heap = listOf())

        assertFalse(tableIsValid(game = game))
    }
}