package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.model.*
import java.util.*

fun createGame(name : String) : Game {
    return Game(name)
}

fun joinGame(game: Game, player: Player) : Boolean {
    return if (GameState.JOINING == game.gameState) {
        game.players[player.id] = player
        true
    } else
        false

}

fun leaveGame(game: Game, player: Player) : Boolean {
    return if (GameState.JOINING == game.gameState) {
        game.players.remove(player.id)
        true
    } else
        false
}

fun startGame(game: Game) : Boolean {
    return if (GameState.JOINING == game.gameState) {
        game.startGame()
        true
    } else
        false
}

fun stopGame(game: Game) : Boolean {
    return if (GameState.JOINING == game.gameState) {
        game.stopGame()
        true
    } else
        false
}

fun playerDrawsFromHeap(game: Game, player: Player) : Boolean {
    return if (game.heap.isNotEmpty()) {
        val tile: Tile = game.heap.random()
        game.heap.remove(tile)
        val tileSet = player.tileToHand(tile)
        game.tileSets[tileSet.id] = tileSet
        true
    } else
        false
}

fun playerPutsTilesOnTable(game: Game, player: Player, tileSetId : UUID) : Boolean {
    if (!player.hand.containsKey(tileSetId))
        return false

    val tileSet = player.hand[tileSetId]!!
    player.hand.remove(tileSetId)

    game.table[tileSet.id] = tileSet

    return true
}

fun splitTileSet(move : Move) : Boolean {
    val tileSetId = move.splitSetId
    val index : Int = move.splitIndex

    val location = when (move.moveLocation) {
        MoveLocation.TABLE -> move.game.table
        MoveLocation.HAND -> move.game.getCurrentPlayer().hand
    }

    if (!location.containsKey(tileSetId))
        return false

    val newSets = location[tileSetId]?.let { split(it, index) }
    if (newSets != null && newSets.size != 2) {
        return false
    }

    for (group in newSets!!) {
        location[group.id] = group
        move.game.tileSets[group.id] = group
    }
    location.remove(tileSetId)
    move.game.tileSets.remove(tileSetId)

    return true
}

fun mergeTileSets(move: Move) : Boolean {
    val location = when (move.moveLocation) {
        MoveLocation.TABLE -> move.game.table
        MoveLocation.HAND -> move.game.getCurrentPlayer().hand
    }
    var newSet = location[move.leftMergeId]?.let { location[move.rightMergeId]?.let { it1 -> merge(it, it1) } }
    move.game.tileSets.remove(move.leftMergeId)
    move.game.tileSets.remove(move.rightMergeId)
    move.game.tileSets[newSet!!.id] = newSet

    return true
}

fun split(tileSet : TileSet, index : Int) : List<TileSet> {
    if (index == 0 || index == tileSet.tiles.size)
        return listOf(tileSet)
    var head = TileSet(tileSet.tiles.subList(0, index))
    var tail = TileSet(tileSet.tiles.subList(index, tileSet.tiles.size))
    return listOf(head, tail)
}

fun merge(left: TileSet, right : TileSet) : TileSet {
    var tiles = mutableListOf<Tile>()
    tiles.addAll(left.tiles)
    tiles.addAll(right.tiles)
    return TileSet(tiles)
}

fun playerEndsTurn(game: Game, player: Player) : Boolean {
    // check if initial play is met
    if (!playerHasInitialPlay(player))
        return false

    game.nextPlayer()
    return true
}

fun playerCanPlay(move: Move) : Boolean {
    return playerIsInGame(move.game, move.player)
            && isCurrentPlayer(move.game, move.player)
}

fun playerCanArrangeHand(move: Move) : Boolean {
    return playerIsInGame(move.game, move.player)
            && (move.moveLocation == MoveLocation.HAND
            || isCurrentPlayer(move.game, move.player))
}

fun playerIsInGame(game: Game, player : Player) : Boolean {
    return game.players.containsKey(player.id)
}

fun isCurrentPlayer(game: Game, player: Player) : Boolean {
    return game.getCurrentPlayer() == player
}

fun playerHasInitialPlay(player: Player) : Boolean {
    return player.initialPlay
}

fun findTileSet(game : Game, id : UUID) : TileSet? {
//    var tileSet : TileSet? = null
//    if (game.table.containsKey(id)) {
//        tileSet = game.table[id]
//    } else {
//        for (player in game.players.values) {
//            if (tileSet != null)
//                break
//            if (player.hand.containsKey(id))
//                tileSet = player.hand[id]
//        }
//    }
//    return tileSet
    return game.tileSets[id]
}

fun isValidTileSet(tileSet : TileSet) : Boolean {
    return isValidGroup(tileSet) || isValidRun(tileSet)
}

fun isValidGroup(tileSet: TileSet) : Boolean {
    return hasEnoughTiles(tileSet.tiles)
            && doesNotExceedGroupLength(tileSet.tiles)
            && allSameTileValue(tileSet.tiles)
            && allUniqueTileColor(tileSet.tiles)
}

fun isValidRun(tileSet: TileSet) : Boolean {
    return hasEnoughTiles(tileSet.tiles)
            && doesNotExceedRunLength(tileSet.tiles)
            && allSameTileColor(tileSet.tiles)
            && allUniqueTileValue(tileSet.tiles)
            && allSequentialTileValue(tileSet.tiles)
}

fun hasEnoughTiles(tiles: List<Tile>) : Boolean {
    return tiles.size >= 3
}

fun doesNotExceedGroupLength(tiles: List<Tile>) : Boolean {
    return tiles.size <= 4
}

fun doesNotExceedRunLength(tiles: List<Tile>) : Boolean {
    return tiles.size <= 13
}

fun allSameTileColor(tiles : List<Tile>) : Boolean {
    return tiles.stream()
        .filter { t -> tileIsRegular(t) }
        .map { t -> t.color }.distinct().count().toInt() == 1
}

fun allSameTileValue(tiles : List<Tile>) : Boolean {
    return tiles.stream()
        .filter { t-> tileIsRegular(t) }
        .map { t -> t.value }
        .distinct()
        .count().toInt() == 1
}

fun allUniqueTileColor(tiles: List<Tile>) : Boolean {
    val distinct = tiles.size - tiles.stream().filter {t-> tileIsJoker(t)}.count().toInt()
    return tiles.stream()
        .filter { t -> tileIsRegular(t) }
        .map { t -> t.color }.distinct().count().toInt() == distinct
}

fun allUniqueTileValue(tiles: List<Tile>) : Boolean {
    val distinct = tiles.size - tiles.stream().filter {t-> tileIsJoker(t)}.count().toInt()
    return tiles.stream()
        .filter { t -> tileIsRegular(t) }
        .map { t -> t.value }.distinct().count().toInt() == distinct
}

fun allSequentialTileValue(tiles: List<Tile>) : Boolean {
    var expectedNumber = tiles[0].value.ordinal
    var isSequential = true

    for (tile in tiles) {
        when (tile.type) {
            TileType.JOKER -> expectedNumber += 1
            TileType.REGULAR -> {
                if ((tile.value.ordinal) != expectedNumber) {
                    isSequential = false
                } else
                    expectedNumber = tile.value.ordinal + 1
            }
        }
    }
    return isSequential
}

fun tileIsJoker(tile: Tile) : Boolean {
    return tile.type == TileType.JOKER
}

fun tileIsRegular(tile: Tile) : Boolean {
    return tile.type == TileType.REGULAR
}