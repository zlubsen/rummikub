package eu.lubsen.rummikub.model

import java.util.UUID

class Move constructor(val game: Game, val player : Player, val moveType: MoveType) {
    var splitIndex : Int = 0
    lateinit var splitSetId : UUID
    lateinit var leftMergeId : UUID
    lateinit var rightMergeId : UUID
    lateinit var tilesToTable : UUID
    lateinit var moveLocation : MoveLocation

    fun setSplit(location: MoveLocation, tileSetId: UUID, index : Int) {
        moveLocation = location
        splitSetId = tileSetId
        splitIndex = index
    }

    fun setMerger(location: MoveLocation, leftId : UUID, rightId : UUID) {
        moveLocation = location
        leftMergeId = leftId
        rightMergeId = rightId
    }

    fun setPutOnTable(tileGroup : UUID) {
        tilesToTable = tileGroup
    }
}