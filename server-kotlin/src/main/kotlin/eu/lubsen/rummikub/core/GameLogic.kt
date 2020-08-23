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
                .addRecipient(recipients = game.players.keys)
        )
    } else
        Failure("Game is not open for joining (${game.gameState}).")
}

fun leaveGame(game: Game, player: Player) : Result<ServerMessage> {
    // TODO add possibility for leaving an ongoing game
    return if (GameState.STARTED != game.gameState) {
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
    val playedInitial = playerHasInitialPlay(player = move.player) || isHeapEmpty(game = move.game)

    return if (playedInitial) {
        when (move.moveType) {
            MoveType.HAND_TO_TABLE -> allowedToPlay.chain(next = ::playerPutsTilesOnTable, arg = move)
            MoveType.TABLE_TO_HAND -> allowedToPlay.chain(next = ::playerPutsTilesInHand, arg = move)
            MoveType.SPLIT -> allowedToArrange.chain(next = ::splitTileSet, arg = move)
            MoveType.MERGE -> allowedToArrange.chain(next = ::mergeTileSets, arg = move)
            MoveType.TAKE_FROM_HEAP -> allowedToPlay.chain(next = ::playerDrawsFromHeap, arg = move)
            MoveType.RESET_TURN -> allowedToPlay.chain(next = ::playerResetsTurn, arg = move)
            MoveType.END_TURN -> allowedToPlay.chain(next = ::playerEndsTurn, arg = move)
        }
    } else
        tryInitialPlayMove(move = move)
}

// TODO - Rule: player can swap a joker for a matching tile from it's rack during initial play.
fun tryInitialPlayMove(move: Move) : Result<MoveResult> {
    val allowedToPlay = playerCanPlay(move = move)
    val arrangeHand = playerCanArrangeHand(move = move)

    return when (move.moveType) {
        MoveType.HAND_TO_TABLE -> allowedToPlay.chain(next = ::playerPutsTilesOnTable, arg = move)
        MoveType.TABLE_TO_HAND -> allowedToPlay.chain(next = ::playerPutsTilesInHand, arg = move)
        MoveType.SPLIT -> arrangeHand.chain(next = ::splitTileSet, arg = move)
        MoveType.MERGE -> arrangeHand.chain(next = ::mergeTileSets, arg = move)
        MoveType.TAKE_FROM_HEAP -> allowedToPlay.chain(next = ::playerDrawsFromHeap, arg = move)
        MoveType.RESET_TURN -> allowedToPlay.chain(next = ::playerResetsTurn, arg = move)
        MoveType.END_TURN -> allowedToPlay.chain(next = ::playerEndsInitialTurn, arg = move)
    }
}
fun playerPutsTilesOnTable(move: Move) : Result<MoveResult> =
    playerPutsTilesOnTable(game = move.game, player = move.player, tileSetId = move.tilesToRelocate)

fun playerPutsTilesInHand(move: Move) : Result<MoveResult> =
    playerPutsTilesInHand(game = move.game, player = move.player, tileSetId = move.tilesToRelocate)

fun playerDrawsFromHeap(move: Move) : Result<MoveResult> =
    playerDrawsFromHeap(game = move.game, player = move.player)

fun playerEndsTurn(move: Move) : Result<MoveResult> =
    playerEndsTurn(game = move.game, player = move.player)

fun playerResetsTurn(move: Move) : Result<MoveResult> =
    playerResetsTurn(game = move.game, player = move.player)

fun playerEndsInitialTurn(move: Move) : Result<MoveResult> =
    playerEndsInitialTurn(game = move.game)

fun moveResponse(game: Game, result: Result<MoveResult>) : Result<List<ServerMessage>> {
    return when(result) {
        is Success -> when (result.result()) {
            is TilesMerged -> {
                val moveResult = result.result() as TilesMerged
                val message: ServerMessage = PlayedTileSetsMerged(
                    eventNumber = 0,
                    move = moveResult
                )
                when (moveResult.location) {
                    MoveLocation.HAND -> message.addRecipient(moveResult.playerId)
                    MoveLocation.TABLE -> message.addRecipient(game.players.keys)
                    MoveLocation.NONE -> return Failure("Incorrect move location set.")
                }
                Success(listOf(message))
            }
            is TilesSplit -> {
                val moveResult = result.result() as TilesSplit
                val message: ServerMessage = PlayedTileSetSplit(
                    eventNumber = 0,
                    move = moveResult
                )
                when (moveResult.location) {
                    MoveLocation.HAND -> message.addRecipient(moveResult.playerId)
                    MoveLocation.TABLE -> message.addRecipient(game.players.keys)
                    MoveLocation.NONE -> return Failure("Incorrect move location set.")
                }
                Success(listOf(message))
            }
            is TurnEnded -> {
                val items = mutableListOf<ServerMessage>()
                items.add(
                    PlayedTurnEnded(
                        eventNumber = 0,
                        move = result.result() as TurnEnded
                    ).addRecipient(recipients = game.players.keys)
                )
                items.addAll(game.players.values
                    .map {
                        GameStateResponse(eventNumber = 0, game = game, player = it)
                            .addRecipient(recipient = it.id)
                    })
                Success(items.toList())
            }
            is PlayerWins -> {
                Success(
                    listOf(
                        GameFinished(eventNumber = 0, game = game).addRecipient(recipients = game.players.keys)
                    )
                )
            }
            is MoveOk -> {
                val messages: List<ServerMessage> = when ((result.result() as MoveOk).type) {
                    MoveType.TAKE_FROM_HEAP -> {
                        val moveOk = result.result() as MoveOk
                        val items = mutableListOf<ServerMessage>()
                        items.add(
                            PlayedTookFromHeap(eventNumber = 0, move = moveOk)
                                .addRecipient(recipient = moveOk.playerId)
                        )
                        items.add(
                            PlayedTurnEnded(
                                eventNumber = 0, move = TurnEnded(
                                    type = MoveType.END_TURN,
                                    playerId = moveOk.playerId,
                                    nextPlayerId = game.getCurrentPlayer().id
                                )
                            )
                                .addRecipient(recipients = game.players.keys)
                        )
                        items.add(
                            PlayerTookFromHeap(
                                eventNumber = 0,
                                move = moveOk
                            ).addRecipient(recipients = game.players.keys.filterNot { it == moveOk.playerId })
                        )
                        items.addAll(game.players.values
                            .map {
                                GameStateResponse(eventNumber = 0, game = game, player = it)
                                    .addRecipient(recipient = it.id)
                            })
                        items
                    }
                    MoveType.TABLE_TO_HAND -> {
                        val moveOk = result.result() as MoveOk
                        listOf(
                            PlayedTilesTableToHand(eventNumber = 0, move = moveOk)
                                .addRecipient(recipient = moveOk.playerId),
                            TableChangedTableToHand(eventNumber = 0, move = moveOk)
                                .addRecipient(recipients = game.players.keys.filterNot { moveOk.playerId == it })
                        )
                    }
                    MoveType.HAND_TO_TABLE -> {
                        val moveOk = result.result() as MoveOk
                        listOf(
                            PlayedTilesHandToTable(eventNumber = 0, move = moveOk)
                                .addRecipient(moveOk.playerId),
                            TableChangedHandToTable(eventNumber = 0, move = moveOk)
                                .addRecipient(recipients = game.players.keys.filterNot { moveOk.playerId == it })
                        )
                    }
                    else -> {
                        listOf(
                            MessageResponse(eventNumber = 0, message = "This response should not be possible...")
                                .addRecipient(game.players.keys)
                        )
                    }
                }
                Success(messages)
            }
            is TurnReset -> {
                val items = mutableListOf<ServerMessage>()
                val message:ServerMessage = MessageResponse(
                    eventNumber = 0,
                    message = "The layout was reset to the start of the turn.")
                    .addRecipient(recipient = game.getCurrentPlayer().id)
                items.add(message)
                items.addAll(game.players.values
                    .map {
                        GameStateResponse(eventNumber = 0, game = game, player = it)
                            .addRecipient(recipient = it.id)
                    })
                Success(items.toList())
            }
        }
        is Failure -> Failure(reason = result.message())
    }
}

fun gameHasValidNoOfPlayers(game: Game) : Boolean {
    return game.players.size in 2..4
}

fun playerDrawsFromHeap(game: Game, player: Player) : Result<MoveResult> {
    endAndResetTurn(game = game)

    return if (!isHeapEmpty(game = game)) {
        val tile: Tile = game.heap.random()
        game.heap.remove(element = tile)
        val tileSet = player.tileToHand(tile = tile)
        game.tileSets[tileSet.id] = tileSet

        Success(value = MoveOk(
            type = MoveType.TAKE_FROM_HEAP,
            playerId = player.id,
            tileSet = tileSet,
            newLocation = MoveLocation.HAND))
    } else {
        // heap is empty
        Success(
            value = TurnEnded(
                type = MoveType.END_TURN,
                playerId = player.id,
                nextPlayerId = game.getCurrentPlayer().id
            )
        )
    }
}

fun isHeapEmpty(game: Game) : Boolean {
    return game.heap.isEmpty()
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
        playerId = player.id,
        tileSet = tileSet,
        newLocation = MoveLocation.TABLE))
}

fun playerPutsTilesInHand(game: Game, player: Player, tileSetId: UUID) : Result<MoveResult> {
    if (!game.table.containsKey(key = tileSetId))
        return Failure(reason = "TileSet is not on table.")

    val tileSet = game.table[tileSetId]!!
    val playedIntersect = tileSet.tiles.filter { tileIsRegular(it) }.intersect(game.turn.tilesIntroduced)
    return if (playedIntersect.size != tileSet.tiles.size)
        Failure(reason = "Not all tiles were in player's hand. ${
            game.turn.tilesIntroduced.joinToString(
                separator = ", ",
                prefix = "Played: (",
                postfix = ")"
            ) { prettyPrintTile(it) }
        }") // tileSet contains tiles not played in this turn
    else {
        game.table.remove(tileSet.id)
        player.hand[tileSetId] = tileSet
        game.turn.tilesIntroduced = game.turn.tilesIntroduced.subtract(playedIntersect).toMutableList()
        Success(value = MoveOk(
            type = MoveType.TABLE_TO_HAND,
            playerId = player.id,
            tileSet = tileSet,
            newLocation = MoveLocation.HAND))
    }
}

fun splitTileSet(move : Move) : Result<MoveResult> {
    val tileSetId = move.sourceSetId
    val index : Int = move.operationIndex

    val location = when (move.moveLocation) {
        MoveLocation.TABLE -> move.game.table
        MoveLocation.HAND -> move.player.hand
        MoveLocation.NONE -> return Failure("Incorrect move location provided.")
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
        playerId = move.player.id,
        leftSet = newSets[0],
        rightSet = newSets[1],
        originalId = tileSetId,
        location = move.moveLocation))
}

fun mergeTileSets(move: Move) : Result<MoveResult> {
    val location = when (move.moveLocation) {
        MoveLocation.TABLE -> move.game.table
        MoveLocation.HAND -> move.player.hand
        MoveLocation.NONE -> return Failure("Incorrect move location set.")
    }
    val newSet = location[move.sourceSetId]?.let {
        location[move.targetSetId]?.let {
                it1 -> merge(source = it, target = it1, index = move.operationIndex) } }
    location.remove(move.sourceSetId)
    location.remove(move.targetSetId)
    location[newSet!!.id] = newSet
    move.game.tileSets.remove(move.sourceSetId)
    move.game.tileSets.remove(move.targetSetId)
    move.game.tileSets[newSet.id] = newSet

    return Success(
        value = TilesMerged(
            type = MoveType.MERGE,
            playerId = move.player.id,
            sourceId = move.sourceSetId,
            targetId = move.targetSetId,
            mergedSet = newSet,
            location = move.moveLocation
        )
    )
}

fun split(tileSet : TileSet, index : Int) : List<TileSet> {
    if (index == 0 || index == tileSet.tiles.size)
        return listOf(tileSet)
    val head = TileSet(tiles = tileSet.tiles.subList(0, index))
    val tail = TileSet(tiles = tileSet.tiles.subList(index, tileSet.tiles.size))
    return listOf(head, tail)
}

fun merge(source: TileSet, target : TileSet, index: Int) : TileSet {
    val tiles = mutableListOf<Tile>()
    tiles.addAll(elements = target.tiles)
    tiles.addAll(index = index, elements = source.tiles)
    return TileSet(tiles)
}

fun playerEndsTurn(game: Game, player: Player) : Result<MoveResult> {
    if (!playerHasInitialPlay(player = player)
        && !playerHasPlayedInTurn(game = game, player = player)
    )
        return Failure(reason = "Player did not play any tiles or met initial play value.")

    if (hasPlayerWon(game = game, player = player)) {
        playerWins(game = game)
        return Success(value = PlayerWins(type = MoveType.END_TURN, playerId = player.id))
    }

    val hasPlayed = playerHasPlayedInTurn(game = game, player = player)
    val tableIsValid = tableIsValid(game = game)

    return if (hasPlayed
        && tableIsValid
    ) {
        endTurn(game = game)
        Success(
            value = TurnEnded(
                type = MoveType.END_TURN,
                playerId = player.id,
                nextPlayerId = game.getCurrentPlayer().id
            )
        )
    } else {
        if (!hasPlayed)
            Failure("Cannot end turn, player did not play tiles to the table.")
        else// if (!tableIsValid)
            Failure("Cannot end turn, table contains invalid melds.")
    }

}

// TODO player cannot win in initial turn
fun playerEndsInitialTurn(game: Game) : Result<MoveResult> {
    return if (tileListValue(game.turn.tilesIntroduced) >= 30 && tableIsValid(game = game)) {
        game.getCurrentPlayer().initialPlay = true
        val playerId = game.getCurrentPlayer().id
        endTurn(game = game)
        val nextPlayerId = game.getCurrentPlayer().id
        Success(value = TurnEnded(
            type = MoveType.END_TURN,
            playerId = playerId,
            nextPlayerId = nextPlayerId))
    } else
        Failure(reason = "Player does not meet initial play value, or the table contains invalid sets.")
}

fun playerResetsTurn(game: Game, player: Player) : Result<MoveResult> {
    return when (player == game.getCurrentPlayer()) {
        true -> {
            resetTurn(game = game)
            Success(TurnReset(MoveType.RESET_TURN))
        }
        false -> {
            Failure(reason = "${player.playerName} is not the current player.")
        }
    }
}

fun endAndResetTurn(game: Game) {
    // reset the turn
    resetTurn(game = game)
    endTurn(game = game)
}

// TODO resetting does not work on initial play: tileset of insufficient value is not placed back in hand from table
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

fun playerWins(game: Game) {
    game.gameState = GameState.FINISHED
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
        Failure(reason = "Player cannot arrange table when not the current player, or did not met initial play value.")
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

fun playerHasPlayedInTurn(game: Game, player: Player) : Boolean {
    return game.getCurrentPlayer() == player
            && game.turn.tilesIntroduced.isNotEmpty()
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
            && noJokerBeforeOne(tiles = tileSet.tiles)
            && noJokerAfterThirteen(tiles = tileSet.tiles)
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

fun noJokerBeforeOne(tiles: List<Tile>) : Boolean {
    return tiles.size > 2 &&
            !(tiles.first().type == TileType.JOKER && tiles[1].number == TileNumber.ONE)
}

fun noJokerAfterThirteen(tiles: List<Tile>) : Boolean {
    return tiles.size > 2 &&
            !(tiles.last().type == TileType.JOKER && tiles[tiles.lastIndex-1].number == TileNumber.THIRTEEN)
}

fun tileIsJoker(tile: Tile) : Boolean {
    return tile.type == TileType.JOKER
}

fun tileIsRegular(tile: Tile) : Boolean {
    return tile.type == TileType.REGULAR
}

// TODO for initial play joker now has a value of zero
fun tileListValue(tiles: List<Tile>) : Int {
    return when(tiles.isEmpty()) {
        true -> 0
        false -> tiles.map { tile ->
            when(tile.type) {
                TileType.REGULAR -> tile.number.ordinal
                TileType.JOKER -> 0
            } }.reduce { sum, element -> sum + element }
    }
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
        else -> 30 // default penalty value
    }
}