package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.model.*

fun getFirstRegularTileSetFromHand(player : Player) : TileSet? {
    var tile = player.hand.values.elementAt(0)
    if (tile.tiles.first().type == TileType.JOKER)
        tile = player.hand.values.elementAt(1)
    if (tile.tiles.first().type == TileType.JOKER)
        tile = player.hand.values.elementAt(2)
    return tile
}

fun startGameWith(game: Game, tableTiles : List<TileSet>, playerHand: List<TileSet>) {
    tableTiles.forEach{ game.table[it.id] = it }
    playerHand.forEach { game.players.values.elementAt(0).hand[it.id] = it }
    game.setTurn()
    game.gameState = GameState.STARTED
}

fun startGameWith(game: Game, tableTiles : List<TileSet>, playerOneHand: List<TileSet>, playerTwoHand: List<TileSet>) {
    playerTwoHand.forEach { game.players.values.elementAt(1).hand[it.id] = it }
    startGameWith(game, tableTiles, playerOneHand)
}