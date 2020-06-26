package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.server.GameListResponse
import eu.lubsen.rummikub.idl.server.MessageResponse
import eu.lubsen.rummikub.idl.server.PlayerListResponse
import eu.lubsen.rummikub.idl.server.ServerMessage
import eu.lubsen.rummikub.model.Lounge
import eu.lubsen.rummikub.model.Move
import eu.lubsen.rummikub.util.Failure
import eu.lubsen.rummikub.util.Result
import eu.lubsen.rummikub.util.Success
import java.util.*

fun handleCreateGame(lounge : Lounge, gameName : String, ownerId : UUID) : Result<ServerMessage> {
    return createGame(lounge = lounge, gameName = gameName, ownerId = ownerId)
}

fun handleRemoveGame(lounge : Lounge, gameName : String, playerId : UUID) : Result<ServerMessage> {
    return if (playerIsOwner(lounge = lounge, gameName = gameName, playerId = playerId)) {
        removeGame(
            lounge = lounge,
            gameName = gameName)
    } else
        Failure("Player does not own game '$gameName'.")
}

fun handleJoinGame(lounge : Lounge, gameName : String, playerId : UUID) : Result<ServerMessage> {
    return if (isValidGameName(lounge = lounge, gameName = gameName))
        joinGame(game = lounge.games[gameName]!!, player = lounge.players[playerId]!!)
    else
        Failure("No game with name '$gameName' exists.")
}

fun handleLeaveGame(lounge : Lounge, gameName : String, playerId : UUID) : Result<ServerMessage> {
    return if (isValidGameName(lounge = lounge, gameName = gameName))
        leaveGame(game = lounge.games[gameName]!!, player = lounge.players[playerId]!!)
    else
        Failure("No game with name '$gameName' exists.")
}

fun handleStartGame(lounge : Lounge, gameName : String, playerId : UUID) : Result<ServerMessage> {
    return if (playerIsOwner(lounge = lounge, gameName = gameName, playerId = playerId))
        startGame(game = lounge.games[gameName]!!)
    else
        Failure("Cannot start game, player is not the owner.")
}

fun handleStopGame(lounge : Lounge, gameName : String, playerId : UUID) : Result<ServerMessage> {
    return if (playerIsOwner(lounge = lounge, gameName = gameName, playerId = playerId))
        stopGame(game = lounge.games[gameName]!!)
    else
        Failure("Cannot start game, player is not the owner.")
}

fun handlePlayerMove(lounge : Lounge, gameName : String, move : Move) : Result<ServerMessage> {
    return if (isValidGameName(lounge = lounge, gameName = gameName))
        moveResponse(game = lounge.games[gameName]!!, result = tryMove(move = move))
    else
        Failure("No game with name '$gameName' exists.")
}

fun handleRequestGameList(lounge : Lounge, playerId : UUID) : Result<ServerMessage> {
    return Success(
        GameListResponse(
            eventNumber = 0,
            games = lounge.listGames()
        ).addRecipient(playerId)
    )
}

fun handleRequestPlayerList(lounge : Lounge, playerId : UUID) : Result<ServerMessage> {
    return Success(
        PlayerListResponse(
            eventNumber = 0,
            players = lounge.listPlayers()
        ).addRecipient(playerId)
    )
}