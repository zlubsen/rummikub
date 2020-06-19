package eu.lubsen.rummikub.model

data class Turn constructor(
    val table: List<TileSet>,
    val playerHand: List<TileSet>) {
    var tilesIntroduced : MutableList<Tile> = mutableListOf()
}