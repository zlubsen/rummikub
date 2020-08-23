package eu.lubsen.rummikub.model

enum class MoveType {
    HAND_TO_TABLE,
    TABLE_TO_HAND,
    MERGE,
    SPLIT,
//    TAKE_JOKER,
    TAKE_FROM_HEAP,
    END_TURN,
    RESET_TURN
}