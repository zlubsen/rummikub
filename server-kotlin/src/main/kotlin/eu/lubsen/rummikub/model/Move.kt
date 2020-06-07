package eu.lubsen.rummikub.model

import java.util.UUID

class Move constructor(val game: Game, val player : Player, val moveType: MoveType) {
    var splitIndex : Int = 0
    lateinit var splitSetId : UUID
    lateinit var leftMergeId : UUID
    lateinit var rightMergeId : UUID
    lateinit var tilesToTable : UUID
    lateinit var moveLocation : MoveLocation

    fun setSplit(groupId: UUID, index : Int) {
        splitSetId = groupId
        splitIndex = index
    }

    fun setMerger(left : UUID, right : UUID) {
        leftMergeId = left
        rightMergeId = right
    }

    fun setPutOnTable(tileGroup : UUID) {
        tilesToTable = tileGroup
    }
}