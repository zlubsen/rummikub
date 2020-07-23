package eu.lubsen.rummikub.model

import java.util.UUID

sealed class MoveResult(open val type : MoveType)

data class TilesMerged(override val type : MoveType, val playerId : UUID, val mergedSet : TileSet, val leftId : UUID, val rightId : UUID, val location: MoveLocation) : MoveResult(type)
data class TilesSplit(override val type : MoveType, val playerId : UUID, val leftSet : TileSet, val rightSet : TileSet, val originalId : UUID, val location: MoveLocation) : MoveResult(type)
data class TurnEnded(override val type : MoveType, val playerId : UUID, val nextPlayerId : UUID) : MoveResult(type)
data class MoveOk(override val type : MoveType, val playerId : UUID, val tileSet: TileSet, val newLocation: MoveLocation) : MoveResult(type)
