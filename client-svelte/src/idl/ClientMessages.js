
function msgCreateGame(playerId, gameId) {
    return {
        messageType : "CreateGame",
        playerId : playerId,
        gameId : gameId
    }
}

function msgRemoveGame(playerId, gameId) {
    return {
        messageType : "RemoveGame",
        playerId : playerId,
        gameId : gameId
    }
}

function msgJoinGame(playerId, gameId) {
    return {
        messageType : "JoinGame",
        playerId : playerId,
        gameId : gameId
    }
}

function msgLeaveGame(playerId, gameId) {
    return {
        messageType : "LeaveGame",
        playerId : playerId,
        gameId : gameId
    }
}

function msgStartGame(playerId, gameId) {
    return {
        messageType : "StartGame",
        playerId : playerId,
        gameId : gameId
    }
}

function msgStopGame(playerId, gameId) {
    return {
        messageType : "StopGame",
        playerId : playerId,
        gameId : gameId
    }
}

function msgRequestPlayerList(playerId) {
    return {
        messageType : "RequestPlayerList",
        playerId : playerId
    }
}

function msgRequestPlayerListForGame(playerId, gameId) {
    return {
        messageType : "RequestPlayerListForGame",
        playerId : playerId,
        gameId : gameId
    }
}

function msgRequestGameList(playerId) {
    return {
        messageType : "RequestGameList",
        playerId : playerId
    }
}

function msgRequestGameState(playerId, gameId) {
    return {
        messageType : "RequestGameState",
        playerId : playerId,
        gameId : gameId
    }
}

function msgMerge(playerId, gameId, sourceId, targetId, index, location) {
    return {
        messageType : "PlayerMove",
        moveType : "MERGE",
        playerId : playerId,
        gameId : gameId,
        sourceId : sourceId,
        targetId : targetId,
        index : index,
        targetLocation : location
    }
}

function msgMoveAndMerge(playerId, gameId, sourceId, targetId, index, sourceLocation, targetLocation) {
    return {
        messageType : "PlayerMove",
        moveType : "MOVE_AND_MERGE",
        playerId : playerId,
        gameId : gameId,
        sourceId : sourceId,
        targetId : targetId,
        index : index,
        sourceLocation : sourceLocation,
        targetLocation : targetLocation
    }
}

function msgSplit(playerId, gameId, tileSetId, index, location) {
    return {
        messageType : "PlayerMove",
        moveType : "SPLIT",
        playerId : playerId,
        gameId : gameId,
        splitSetId : tileSetId,
        index : index,
        moveLocation : location
    }
}

function msgHandToTable(playerId, gameId, tileSetId) {
    return {
        messageType : "PlayerMove",
        moveType : "HAND_TO_TABLE",
        playerId : playerId,
        gameId : gameId,
        tileSetId : tileSetId,
    }
}

function msgTableToHand(playerId, gameId, tileSetId) {
    return {
        messageType : "PlayerMove",
        moveType : "TABLE_TO_HAND",
        playerId : playerId,
        gameId : gameId,
        tileSetId : tileSetId,
    }
}

function msgEndTurn(playerId, gameId) {
    return {
        messageType : "PlayerMove",
        moveType : "END_TURN",
        playerId : playerId,
        gameId : gameId
    }
}

function msgResetTurn(playerId, gameId) {
    return {
        messageType : "PlayerMove",
        moveType : "RESET_TURN",
        playerId : playerId,
        gameId : gameId
    }
}

function msgTakeFromHeap(playerId, gameId) {
    return {
        messageType : "PlayerMove",
        moveType : "TAKE_FROM_HEAP",
        playerId : playerId,
        gameId : gameId
    }
}

// TODO messages for PlayerMove

export {
    msgCreateGame,
    msgRemoveGame,
    msgJoinGame,
    msgLeaveGame,
    msgStartGame,
    msgStopGame,
    msgRequestGameList,
    msgRequestPlayerList,
    msgRequestPlayerListForGame,
    msgRequestGameState,
    msgMerge,
    msgMoveAndMerge,
    msgSplit,
    msgHandToTable,
    msgTableToHand,
    msgEndTurn,
    msgResetTurn,
    msgTakeFromHeap
};
