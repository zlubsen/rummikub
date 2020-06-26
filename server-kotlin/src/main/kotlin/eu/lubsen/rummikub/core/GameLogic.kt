package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.server.*
import eu.lubsen.rummikub.model.*
import eu.lubsen.rummikub.util.Failure
import eu.lubsen.rummikub.util.Result
import eu.lubsen.rummikub.util.Success
import java.util.UUID

fun joinGame(game: Game, player: Player) : Result<ServerMessage> {
    return if (GameState.JOINING == game.gameState) {
        game.players[player.id] = player
        Success(
            PlayerJoinedGame(
                eventNumber = 0,
                game = game,
                player = player
            )
        )
    } else
        Failure("Game is not open for joining (${game.gameState}).")
}

fun leaveGame(game: Game, player: Player) : Result<ServerMessage> {
    // TODO add possibility for leaving an ongoing game
    return if (GameState.JOINING == game.gameState) {
        game.players.remove(player.id)
        Success(
            PlayerLeftGame(eventNumber = 0, game = game, player = player)
                .addRecipient(recipients = game.players.keys)
                .addRecipient(recipient = player.id)
        )
    } else
        Failure("Cannot leave an ongoing game (${game.gameState}).")
}

fun startGame(game: Game) : Result<ServerMessage> {
    return if (GameState.JOINING != game.gameState) {
        Failure("Game is not in a valid state to start (${game.gameState}).")
    } else if (!gameHasValidNoOfPlayers(game = game)) {
        Failure("Invalid number of players in game (${game.players.size}).")
    } else {
        game.startGame()
        Success(
            GameStarted(eventNumber = 0, game = game)
                .addRecipient(recipients = game.players.keys)
        )
    }
}

fun stopGame(game: Game) : Result<ServerMessage> {
    return if (GameState.STARTED == game.gameState) {
        game.stopGame()
        Success(
            GameStopped(eventNumber = 0, game = game)
                .addRecipient(recipients = game.players.keys)
        )
    } else
        Failure("Game is not in a valid state to stop (${game.gameState}).")
}

fun tryMove(move: Move) : Result<MoveResult> {
    assert(GameState.STARTED == move.game.gameState)
    val allowedToPlay = playerCanPlay(move = move)
    val allowedToArrange = playerCanArrange(move = move)
    val playedInitial = playerHasInitialPlay(player = move.player)

    if (playedInitial) {
        return when (move.moveType) {
                MoveType.HAND_TO_TABLE -> allowedToPlay.chain(next = playerPutsTilesOnTable(
                    game = move.game,
                    player = move.player,
                    tileSetId = move.tilesToRelocate
                ))
                MoveType.TABLE_TO_HAND -> allowedToPlay.chain(next = playerPutsTilesInHand(
                    game = move.game,
                    player = move.player,
                    tileSetId = move.tilesToRelocate
                ))
                MoveType.SPLIT -> allowedToArrange.chain(next = splitTileSet(move = move))
                MoveType.MERGE -> allowedToArrange.chain(next = mergeTileSets(move = move))
                MoveType.TAKE_FROM_HEAP -> allowedToPlay.chain(next = playerDrawsFromHeap(game = move.game, player = move.player))
                MoveType.END_TURN -> allowedToPlay.chain(next = playerEndsTurn(game = move.game, player = move.player))
            }
    } else
        return tryInitialPlayMove(move = move)
}

fun tryInitialPlayMove(move: Move) : Result<MoveResult> {
    val allowedToPlay = playerCanPlay(move = move)
    val arrangeHand = playerCanArrangeHand(move = move)

    return when (move.moveType) {
        MoveType.HAND_TO_TABLE -> playerPutsTilesOnTable(game = move.game, player = move.player, tileSetId = move.tilesToRelocate)
        MoveType.TABLE_TO_HAND -> allowedToPlay.chain(next = playerPutsTilesInHand(game = move.game, player = move.player, tileSetId = move.tilesToRelocate))
        MoveType.SPLIT -> arrangeHand.chain(next = splitTileSet(move = move))
        MoveType.MERGE -> arrangeHand.chain(next = mergeTileSets(move = move))
        MoveType.TAKE_FROM_HEAP -> allowedToPlay.chain(next = playerDrawsFromHeap(game = move.game, player = move.player))
        MoveType.END_TURN -> allowedToPlay.chain(next = playerEndsInitialTurn(game = move.game))
    }
}

fun moveResponse(game: Game, result: Result<MoveResult>) : Result<ServerMessage> {
    return when(result) {
        is Success -> when(result.result()) {
            is TilesMerged -> {
                val message : ServerMessage = PlayedTileSetsMerged(
                    eventNumber = 0,
                    move = result.result() as TilesMerged)
                when((result.result() as TilesMerged).location) {
                    MoveLocation.HAND -> message.addRecipient(game.getCurrentPlayer().id)
                    MoveLocation.TABLE -> message.addRecipient(game.players.keys)
                }
                Success(message)
            }
            is TilesSplit -> {
                val message : ServerMessage = PlayedTileSetSplit(
                    eventNumber = 0,
                    move = result.result() as TilesSplit)
                when((result.result() as TilesSplit).location) {
                    MoveLocation.HAND -> message.addRecipient(game.getCurrentPlayer().id)
                    MoveLocation.TABLE -> message.addRecipient(game.players.keys)
                }
                Success(message)
            }
            is TurnEnded -> {
                val message : ServerMessage = PlayedTurnEnded(
                    eventNumber = 0,
                    move = result.result() as TurnEnded
                )
                Success(message)
            }
            is MoveOk -> {
                val message : ServerMessage = when ((result.result() as MoveOk).type) {
                    MoveType.TAKE_FROM_HEAP -> {
                        PlayedTookFromHeap(eventNumber = 0, move = result.result() as MoveOk)
                    }
                    MoveType.TABLE_TO_HAND -> {
                        PlayedTilesTableToHand(eventNumber = 0, move = result.result() as MoveOk)
                    }
                    MoveType.HAND_TO_TABLE -> {
                        PlayedTilesHandToTable(eventNumber = 0, move = result.result() as MoveOk)
                    }
                    else -> {MessageResponse(eventNumber = 0, message = "This should not be possible...")}
                }
                Success(message)
            }
        }
        is Failure -> Failure(reason = result.message())
    }
}

fun gameHasValidNoOfPlayers(game: Game) : Boolean {
    return game.players.size in 2..4
}

fun playerDrawsFromHeap(game: Game, player: Player) : Result<MoveResult> {
    return if (game.heap.isNotEmpty()) {
        endAndResetTurn(game = game)

        val tile: Tile = game.heap.random()
        game.heap.remove(element = tile)
        val tileSet = player.tileToHand(tile = tile)
        game.tileSets[tileSet.id] = tileSet

        Success(value = MoveOk(
            type = MoveType.TAKE_FROM_HEAP,
            tileSet = tileSet,
            newLocation = MoveLocation.HAND))
    } else
        Success(value = TurnEnded) // heap is empty
}

fun playerPutsTilesOnTable(game: Game, player: Player, tileSetId : UUID) : Result<MoveResult> {
    if (!player.hand.containsKey(key = tileSetId))
        return Failure(reason = "TileSet is not in player's hand.")

    val tileSet = player.hand[tileSetId]!!
    player.hand.remove(key = tileSetId)

    game.table[tileSet.id] = tileSet
    tileSet.tiles.filter { tileIsRegular(it) }.forEach { game.turn.tilesIntroduced.add(element = it) }

    return Success(value = MoveOk(
        type = MoveType.HAND_TO_TABLE,
        tileSet = tileSet,
        newLocation = MoveLocation.TABLE))
}

// TODO test function
fun playerPutsTilesInHand(game: Game, player: Player, tileSetId: UUID) : Result<MoveResult> {
    if (!game.table.containsKey(key = tileSetId))
        return Failure(reason = "TileSet is not on table.")

    val tileSet = game.table[tileSetId]!!
    val playedIntersect = tileSet.tiles.filter { tileIsRegular(it) }.intersect(game.turn.tilesIntroduced)
    return if (playedIntersect.size == tileSet.tiles.size)
        Failure(reason = "Not all tiles were in player's hand: ${playedIntersect.joinToString { ", " }}") // tileSet contains tiles not played in this move
    else {
        game.table.remove(tileSet.id)
        player.hand[tileSetId] = tileSet
        Success(value = MoveOk(
            type = MoveType.TABLE_TO_HAND,
            tileSet = tileSet,
            newLocation = MoveLocation.HAND))
    }
}

fun splitTileSet(move : Move) : Result<MoveResult> {
    val tileSetId = move.splitSetId
    val index : Int = move.splitIndex

    val location = when (move.moveLocation) {
        MoveLocation.TABLE -> move.game.table
        MoveLocation.HAND -> move.game.getCurrentPlayer().hand
    }

    if (!location.containsKey(key = tileSetId))
        return Failure(reason = "TileSet not found in ${move.moveLocation}.")

    val newSets = split(tileSet = location[tileSetId]!!, index = index)
    if (newSets.size != 2) {
        return Failure(reason = "Split resulted in a something else than 2 sets.")
    }

    newSets.forEach {
        location[it.id] = it
        move.game.tileSets[it.id] = it
    }

    location.remove(key = tileSetId)
    move.game.tileSets.remove(key = tileSetId)

    return Success(value = TilesSplit(
        type = MoveType.SPLIT,
        leftSet = newSets[0],
        rightSet = newSets[1],
        originalId = tileSetId,
        location = move.moveLocation))
}

fun mergeTileSets(move: Move) : Result<MoveResult> {
    val location = when (move.moveLocation) {
        MoveLocation.TABLE -> move.game.table
        MoveLocation.HAND -> move.game.getCurrentPlayer().hand
    }
    val newSet = location[move.leftMergeId]?.let { location[move.rightMergeId]?.let { it1 -> merge(left = it, right = it1) } }
    move.game.tileSets.remove(move.leftMergeId)
    move.game.tileSets.remove(move.rightMergeId)
    move.game.tileSets[newSet!!.id] = newSet

    return Success(value = TilesMerged(
        type = MoveType.MERGE,
        leftId = move.leftMergeId,
        rightId = move.rightMergeId,
        mergedSet = newSet,
        location = move.moveLocation))
}

fun split(tileSet : TileSet, index : Int) : List<TileSet> {
    if (index == 0 || index == tileSet.tiles.size)
        return listOf(tileSet)
    val head = TileSet(tiles = tileSet.tiles.subList(0, index))
    val tail = TileSet(tiles = tileSet.tiles.subList(index, tileSet.tiles.size))
    return listOf(head, tail)
}

fun merge(left: TileSet, right : TileSet) : TileSet {
    val tiles = mutableListOf<Tile>()
    tiles.addAll(elements = left.tiles)
    tiles.addAll(elements = right.tiles)
    return TileSet(tiles)
}

fun playerEndsTurn(game: Game, player: Player) : Result<MoveResult> {
    // check if the player played tiles
    if (!playerHasInitialPlay(player = player)
        && !playerHasPlayedInTurn(player = player))
        return Failure(reason = "Player did not play any tiles or met initial play value.")

    if(!hasPlayerWon(game = game, player = player)
        && playerHasPlayedInTurn(player = player)
        && tableIsValid(game = game))
        endTurn(game = game)
    return Success(value = TurnEnded)
}

fun playerEndsInitialTurn(game: Game) : Result<MoveResult> {
    return if (tileListValue(game.turn.tilesIntroduced) > 30 && tableIsValid(game = game)) {
        endTurn(game = game)
        Success(value = TurnEnded)
    } else
        Failure(reason = "Player does not meet initial play value, or the table contains invalid sets.")
}

fun endAndResetTurn(game: Game) {
    // reset the turn
    resetTurn(game = game)
    endTurn(game = game)
}

fun resetTurn(game: Game) {
    if (game.turn.tilesIntroduced.isNotEmpty()) {
        game.table.clear()
        game.turn.table.forEach { game.table[it.id] = it }
        game.getCurrentPlayer().hand.clear()
        game.turn.playerHand.forEach { game.getCurrentPlayer().hand[it.id] = it}
    }
}

fun endTurn(game: Game) {
    game.nextPlayer()
    game.setTurn()
}

// TODO test function
fun hasPlayerWon(game: Game, player: Player) : Boolean {
    val allValid = game.table.isNotEmpty()
            && tableIsValid(game = game)
    val playerHandEmpty = player.hand.isEmpty()

    return allValid && playerHandEmpty
}

fun playerCanPlay(move: Move) : Result<Boolean> {
    return if (playerIsInGame(game = move.game, player = move.player)
            && isCurrentPlayer(game = move.game, player = move.player))
        Success(value = true)
    else
        Failure(reason = "Player is not the current player (or not in the game).")
}

fun playerCanArrange(move: Move) : Result<Boolean> {
    return if( playerCanArrangeHand(move = move).isSuccess()
            || isCurrentPlayer(game = move.game, player = move.player))
        Success(value = true)
    else
        Failure("Player is not the current player.")
}

fun playerCanArrangeHand(move: Move) : Result<Boolean> {
    return if (playerIsInGame(game = move.game, player = move.player)
            && (move.moveLocation == MoveLocation.HAND))
        Success(value = true)
    else
        Failure(reason = "Player cannot arrange table when not the current player.")
}

fun playerIsInGame(game: Game, player : Player) : Boolean {
    return game.players.containsKey(key = player.id)
}

fun isCurrentPlayer(game: Game, player: Player) : Boolean {
    return game.getCurrentPlayer() == player
}

fun playerHasInitialPlay(player: Player) : Boolean {
    return player.initialPlay
}

fun playerHasPlayedInTurn(player: Player) : Boolean {
    return player.hasPlayedInTurn
}

fun tableIsValid(game: Game) : Boolean {
    return game.table.values
        .map { tileSet ->  isValidTileSet(tileSet) }
        .fold(true) { sum, element -> sum && element }
}

fun findTileSet(game : Game, id : UUID) : TileSet? {
    return game.tileSets[id]
}

fun isValidTileSet(tileSet : TileSet) : Boolean {
    return isValidGroup(tileSet = tileSet) || isValidRun(tileSet = tileSet)
}

fun isValidGroup(tileSet: TileSet) : Boolean {
    return hasEnoughTiles(tiles = tileSet.tiles)
            && doesNotExceedGroupLength(tiles = tileSet.tiles)
            && allSameTileNumber(tiles = tileSet.tiles)
            && allUniqueTileColor(tiles = tileSet.tiles)
}

fun isValidRun(tileSet: TileSet) : Boolean {
    return hasEnoughTiles(tiles = tileSet.tiles)
            && doesNotExceedRunLength(tiles = tileSet.tiles)
            && allSameTileColor(tiles = tileSet.tiles)
            && allUniqueTileNumber(tiles = tileSet.tiles)
            && allSequentialTileNumber(tiles = tileSet.tiles)
}

fun hasEnoughTiles(tiles: List<Tile>) : Boolean {
    return tiles.size >= 3
}

fun doesNotExceedGroupLength(tiles: List<Tile>) : Boolean {
    return tiles.size <= 4
}

fun doesNotExceedRunLength(tiles: List<Tile>) : Boolean {
    return tiles.size <= 13
}

fun allSameTileColor(tiles : List<Tile>) : Boolean {
    return tiles
        .filter { t -> tileIsRegular(tile = t) }
        .map { t -> t.color }.distinct().count() == 1
}

fun allSameTileNumber(tiles : List<Tile>) : Boolean {
    return tiles
        .filter { t-> tileIsRegular(tile = t) }
        .map { t -> t.number }
        .distinct()
        .count() == 1
}

fun allUniqueTileColor(tiles: List<Tile>) : Boolean {
    val distinct = tiles.size - tiles.filter { t-> tileIsJoker(tile = t)}.count()
    return tiles
        .filter { t -> tileIsRegular(tile = t) }
        .map { t -> t.color }.distinct().count() == distinct
}

fun allUniqueTileNumber(tiles: List<Tile>) : Boolean {
    val regularCount = tiles.size - tiles.filter {t-> tileIsJoker(tile = t)}.count()
    return tiles
        .filter { t -> tileIsRegular(tile = t) }
        .map { t -> t.number }.distinct().count() == regularCount
}

fun allSequentialTileNumber(tiles: List<Tile>) : Boolean {
    return when(true) {
        tiles.first().type == TileType.JOKER ->
            allSequentialTileNumber(tiles = tiles.drop(1))
        else ->
            allSequentialTileNumberRec(
                tiles = tiles.drop(1),
                expected = tiles.first().number.ordinal + 1)
    }
}

fun allSequentialTileNumberRec(tiles: List<Tile>, expected : Int) : Boolean {
    return tiles.isEmpty()
            || when(tiles.first().type) {
                TileType.JOKER -> allSequentialTileNumberRec(
                    tiles = tiles.drop(1),
                    expected = expected + 1)
                TileType.REGULAR ->
                    (tiles.first().number.ordinal == expected)
                            && allSequentialTileNumberRec(
                        tiles = tiles.drop(1),
                        expected = expected + 1)
    }
}

fun tileIsJoker(tile: Tile) : Boolean {
    return tile.type == TileType.JOKER
}

fun tileIsRegular(tile: Tile) : Boolean {
    return tile.type == TileType.REGULAR
}

// TODO for initial play joker now has a value of zero
// TODO test function
fun tileListValue(tiles: List<Tile>) : Int {
    return tiles.map { tile ->
        when(tile.type) {
            TileType.REGULAR -> tile.number.ordinal
            TileType.JOKER -> 0
        } }.reduce { sum, element -> sum + element }
}

fun tileSetValue(tileSet: TileSet) : Int {
    return if (!isValidTileSet(tileSet))
        0
    else
        tileSet.tiles.map { tile ->
            when(tile.type) {
                TileType.REGULAR -> tile.number.ordinal
                TileType.JOKER -> determineJokerValue(
                    tileSet = tileSet,
                    jokerIndex = tileSet.tiles.indexOf(tile))
    } }.reduce { sum, element -> sum + element }
}

fun determineJokerValue(tileSet: TileSet, jokerIndex : Int) : Int {
    return when(true) {
        isValidGroup(tileSet = tileSet) ->
            tileSet.tiles.first { t -> tileIsRegular(t) }.number.ordinal
        isValidRun(tileSet = tileSet) -> {
            val referenceIndex = tileSet.tiles.indexOf(tileSet.tiles.first { t -> tileIsRegular(t) })
            val offset = jokerIndex - referenceIndex
            tileSet.tiles[referenceIndex].number.ordinal + offset
        }
        else -> 0
    }
}