package eu.lubsen.rummikub.model

import java.util.UUID

sealed class MoveResult(open val type : MoveType)

data class TilesMerged(override val type : MoveType, val mergedSet : TileSet, val leftId : UUID, val rightId : UUID, val location: MoveLocation) : MoveResult(type)
data class TilesSplit(override val type : MoveType, val leftSet : TileSet, val rightSet : TileSet, val originalId : UUID, val location: MoveLocation) : MoveResult(type)
object TurnEnded : MoveResult(MoveType.END_TURN)
data class MoveOk(override val type : MoveType, val tileSet: TileSet, val newLocation: MoveLocation) : MoveResult(type)
