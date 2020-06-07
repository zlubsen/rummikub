package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.model.TileColor
import eu.lubsen.rummikub.model.TileNumber
import eu.lubsen.rummikub.model.TileType
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ParserTest {

    @Test
    fun parseSingleTileRegular() {
        val input = "Bla8"
        val tileSet = parseTileSet(input)
        assertTrue(tileSet.tiles.size == 1)
        assertTrue(tileSet.tiles[0].type == TileType.REGULAR)
        assertTrue(tileSet.tiles[0].value == TileNumber.EIGHT)
        assertTrue(tileSet.tiles[0].color == TileColor.BLACK)
    }

    @Test
    fun parseSingleTileJoker() {
        val input = "J"
        val tileSet = parseTileSet(input)
        assertTrue(tileSet.tiles.size == 1)
        assertTrue(tileSet.tiles[0].type == TileType.JOKER)
        assertTrue(tileSet.tiles[0].value == TileNumber.ANY)
        assertTrue(tileSet.tiles[0].color == TileColor.ANY)
    }

    @Test
    fun parseTileSet() {
        val input = "Bla4-Blu4-Red4-Yel4"
        val tileSet = parseTileSet(input)
        assertTrue(tileSet.tiles.size == 4)
        assertTrue(tileSet.tiles[0].type == TileType.REGULAR)
        assertTrue(tileSet.tiles[0].value == TileNumber.FOUR)
        assertTrue(tileSet.tiles[0].color == TileColor.BLACK)
        assertTrue(tileSet.tiles[1].type == TileType.REGULAR)
        assertTrue(tileSet.tiles[1].value == TileNumber.FOUR)
        assertTrue(tileSet.tiles[1].color == TileColor.BLUE)
        assertTrue(tileSet.tiles[2].type == TileType.REGULAR)
        assertTrue(tileSet.tiles[2].value == TileNumber.FOUR)
        assertTrue(tileSet.tiles[2].color == TileColor.RED)
        assertTrue(tileSet.tiles[3].type == TileType.REGULAR)
        assertTrue(tileSet.tiles[3].value == TileNumber.FOUR)
        assertTrue(tileSet.tiles[3].color == TileColor.YELLOW)
    }

    @Test
    fun parseTileSetWithJoker() {
        val input = "Blu1-J-Blu3"
        val tileSet = parseTileSet(input)
        assertTrue(tileSet.tiles.size == 3)
        assertTrue(tileSet.tiles[0].type == TileType.REGULAR)
        assertTrue(tileSet.tiles[0].value == TileNumber.ONE)
        assertTrue(tileSet.tiles[0].color == TileColor.BLUE)
        assertTrue(tileSet.tiles[1].type == TileType.JOKER)
        assertTrue(tileSet.tiles[1].value == TileNumber.ANY)
        assertTrue(tileSet.tiles[1].color == TileColor.ANY)
        assertTrue(tileSet.tiles[2].type == TileType.REGULAR)
        assertTrue(tileSet.tiles[2].value == TileNumber.THREE)
        assertTrue(tileSet.tiles[2].color == TileColor.BLUE)
    }
}