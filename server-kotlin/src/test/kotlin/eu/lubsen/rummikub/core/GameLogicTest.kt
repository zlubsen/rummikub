package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GameLogicTest {

    @Test
    fun playerDrawsFromHeap() {
        var game = Game("drawTest")
        var player1 = Player("tester1")
        var player2 = Player("tester2")
        game.addPlayer(player1)
        game.addPlayer(player2)
        game.startGame()
        var heapSize = game.heap.size

        assertEquals(14, player1.hand.size)
        assertEquals(14, player2.hand.size)
        assertTrue(playerDrawsFromHeap(game, player1))
        assertEquals(15, player1.hand.size)
        assertEquals(14, player2.hand.size)
        assertEquals(heapSize - 1, game.heap.size)
    }

    @Test
    fun playerPutsTilesOnTable() {
        var game = Game("handToTableTest")
        var player1 = Player("tester1")
        game.addPlayer(player1)
        game.startGame()
        val tileSet = getFirstTileSetFromHand(player1)

        assertNotNull(tileSet)
        assertTrue(game.table.isEmpty())
        assertTrue(player1.hand.containsKey(tileSet!!.id))

        playerPutsTilesOnTable(game, player1, tileSet!!.id)

        assertTrue(game.table.containsKey(tileSet!!.id))
        assertFalse(player1.hand.containsKey(tileSet!!.id))
    }

    private fun getFirstTileSetFromHand(player : Player) : TileSet? {
        var tileSet : TileSet? = null
        for (set in player.hand.values) {
            if (tileSet == null)
                tileSet = set
            else
                break
        }
        return tileSet
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
    fun merge() {
        val left = parseTileSet("Bla1")
        val right = parseTileSet("Bla2-Bla3")
        val merged = merge(left, right)
        assertEquals(parseTileSet("Bla1-Bla2-Bla3"), merged)
    }

    @Test
    fun mergeWithEmpty() {
        val left = TileSet(listOf())
        val right = parseTileSet("Yel10-Yel11")
        val merged = merge(left, right)
        assertEquals(parseTileSet("Yel10-Yel11"), merged)
        assertNotEquals(right.id, merged.id)
    }

    @Test
    fun playerEndsTurn() {
        var game = Game("endTurnTest")
        var player1 = Player("tester1")
        player1.initialPlay = true
        var player2 = Player("tester2")
        player2.initialPlay = true
        var player3 = Player("tester3")
        player3.initialPlay = true
        game.addPlayer(player1)
        game.addPlayer(player2)
        game.addPlayer(player3)
        game.startGame()

        assertEquals(player1, game.getCurrentPlayer())

        playerEndsTurn(game, player1)

        assertEquals(player2, game.getCurrentPlayer())

        playerEndsTurn(game, player2)

        assertEquals(player3, game.getCurrentPlayer())

        playerEndsTurn(game, player3)

        assertEquals(player1, game.getCurrentPlayer())
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
    fun allSameTileValue() {
        val input = "Bla4-Blu4-Red4-Yel4"
        val tileSet = parseTileSet(input)
        assertTrue(allSameTileValue(tileSet.tiles))
    }

    @Test
    fun notAllSameTileValue() {
        val input = "Bla4-Blu4-Red4-Yel5"
        val tileSet = parseTileSet(input)
        assertFalse(allSameTileValue(tileSet.tiles))
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
    fun allUniqueTileValue() {
        val input = "Bla1-Bla2-Bla3-Bla4"
        val tileSet = parseTileSet(input)
        assertTrue(allUniqueTileValue(tileSet.tiles))
    }

    @Test
    fun allUniqueTileValueWithJoker() {
        val input = "Bla4-Bla5-J"
        val tileSet = parseTileSet(input)
        assertTrue(allUniqueTileValue(tileSet.tiles))
    }

    @Test
    fun notAllUniqueTileValue() {
        val input = "Bla4-Bla5-Bla5"
        val tileSet = parseTileSet(input)
        assertFalse(allUniqueTileColor(tileSet.tiles))
    }

    @Test
    fun allSequentialTileValue() {
        val input = "Bla1-Bla2-Bla3-Bla4"
        val tileSet = parseTileSet(input)
        assertTrue(allSequentialTileValue(tileSet.tiles))
    }

    @Test
    fun allSequentialTileValueWithJoker() {
        val input = "Bla1-Bla2-J-Bla4"
        val tileSet = parseTileSet(input)
        assertTrue(allSequentialTileValue(tileSet.tiles))
    }

    @Test
    fun notAllSequentialTileValue() {
        val input = "Bla1-Bla2-Bla4"
        val tileSet = parseTileSet(input)
        assertFalse(allSequentialTileValue(tileSet.tiles))
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
}