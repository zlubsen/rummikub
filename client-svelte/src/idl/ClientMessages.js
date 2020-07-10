
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

function msgRequestGameList(playerId) {
    return {
        messageType : "RequestGameList",
        playerId : playerId
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
    msgRequestPlayerList
};
