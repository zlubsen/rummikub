package eu.lubsen.rummikub.model

import java.util.UUID

class Move constructor(val game: Game, val player : Player, val moveType: MoveType) {
    var operationIndex : Int = 0
    lateinit var sourceSetId : UUID
    lateinit var targetSetId : UUID
    lateinit var tilesToRelocate : UUID
    var moveLocation = MoveLocation.NONE

    fun setSplit(location: MoveLocation, tileSetId: UUID, index : Int) {
        moveLocation = location
        sourceSetId = tileSetId
        operationIndex = index
    }

    fun setMerger(location: MoveLocation, sourceId : UUID, targetId : UUID, index: Int) {
        moveLocation = location
        sourceSetId = sourceId
        targetSetId = targetId
        operationIndex = index
    }
}