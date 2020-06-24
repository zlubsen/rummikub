package eu.lubsen.rummikub.model

import java.util.UUID

sealed class MoveResult

data class TilesMerged(val mergedSet : TileSet, val leftId : UUID, val rightId : UUID, val location: MoveLocation) : MoveResult()
data class TilesSplit(val leftSet : TileSet, val rightSet : TileSet, val originalId : UUID, val location: MoveLocation) : MoveResult()
object TurnEnded : MoveResult()
data class MoveOk(val tileSet: TileSet, val newLocation: MoveLocation) : MoveResult()
