package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.server.*
import eu.lubsen.rummikub.model.*
import java.util.UUID

fun joinGame(game: Game, player: Player) : Result<ServerMessage> {
    return if (GameState.JOINING == game.gameState) {
        game.players[player.id] = player
        Success(PlayerJoinedGame(eventNumber = 0, game = game, player = player))
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
                .addRecipient(recipient = player.id))
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
                .addRecipient(recipients = game.players.keys))
    }
}

fun stopGame(game: Game) : Result<ServerMessage> {
    return if (GameState.STARTED == game.gameState) {
        game.stopGame()
        Success(GameStopped(eventNumber = 0, game = game)
            .addRecipient(recipients = game.players.keys))
    } else
        Failure("Game is not in a valid state to stop (${game.gameState}).")
}

fun tryMove(move: Move) : Result<ServerMessage> {
    assert(GameState.STARTED == move.game.gameState)
    val allowedToPlay = playerCanPlay(move = move)
    val allowedToArrange = playerCanArrange(move = move)
    val playedInitial = playerHasInitialPlay(player = move.player)

    if (playedInitial) {
        // TODO improve result messaging for moves
        return if (when (move.moveType) {
                MoveType.HAND_TO_TABLE -> allowedToPlay && playerPutsTilesOnTable(
                    game = move.game,
                    player = move.player,
                    tileSetId = move.tilesToRelocate
                )
                MoveType.TABLE_TO_HAND -> allowedToPlay && playerPutsTilesInHand(
                    game = move.game,
                    player = move.player,
                    tileSetId = move.tilesToRelocate
                )
                MoveType.SPLIT -> allowedToArrange && splitTileSet(move = move)
                MoveType.MERGE -> allowedToArrange && mergeTileSets(move = move)
                MoveType.TAKE_FROM_HEAP -> allowedToPlay && playerDrawsFromHeap(game = move.game, player = move.player)
                MoveType.END_TURN -> allowedToPlay && playerEndsTurn(game = move.game, player = move.player)
            }
        )
        // TODO Create proper move response message
            Success(
                MessageResponse(eventNumber = 0, message = "todo")
                    .addRecipient(recipients = move.game.players.keys)
            )
        else
            Failure("Invalid move.")
    } else
        return tryInitialPlayMove(move = move)
}

fun tryInitialPlayMove(move: Move) : Result<ServerMessage> {
    val allowedToPlay = playerCanPlay(move = move)
    val arrangeHand = playerCanArrangeHand(move = move)

    val result = when (move.moveType) {
        MoveType.HAND_TO_TABLE -> playerPutsTilesOnTable(game = move.game, player = move.player, tileSetId = move.tilesToRelocate)
        MoveType.TABLE_TO_HAND -> allowedToPlay && playerPutsTilesInHand(game = move.game, player = move.player, tileSetId = move.tilesToRelocate)
        MoveType.SPLIT -> arrangeHand && splitTileSet(move = move)
        MoveType.MERGE -> arrangeHand && mergeTileSets(move = move)
        MoveType.TAKE_FROM_HEAP -> allowedToPlay && playerDrawsFromHeap(game = move.game, player = move.player)
        MoveType.END_TURN -> allowedToPlay && playerEndsInitialTurn(game = move.game)
    }
    when (result) {
        is Success<Boolean> -> {}
        is Success<UUID> -> {}
        is Failure -> {}
    }
        Success(
            MessageResponse(eventNumber = 0, message = "todo")
                .addRecipient(recipients = move.game.players.keys)
        )
    else
        Failure("Invalid move.")
}

fun gameHasValidNoOfPlayers(game: Game) : Boolean {
    return game.players.size in 2..4
}

fun playerDrawsFromHeap(game: Game, player: Player) : Boolean {
    return if (game.heap.isNotEmpty()) {
        endAndResetTurn(game = game)

        val tile: Tile = game.heap.random()
        game.heap.remove(element = tile)
        val tileSet = player.tileToHand(tile = tile)
        game.tileSets[tileSet.id] = tileSet

        true
    } else
        false
}

fun playerPutsTilesOnTable(game: Game, player: Player, tileSetId : UUID) : Boolean {
    if (!player.hand.containsKey(key = tileSetId))
        return false

    val tileSet = player.hand[tileSetId]!!
    player.hand.remove(key = tileSetId)

    game.table[tileSet.id] = tileSet
    tileSet.tiles.filter { tileIsRegular(it) }.forEach { game.turn.tilesIntroduced.add(element = it) }

    return true
}

// TODO test function
fun playerPutsTilesInHand(game: Game, player: Player, tileSetId: UUID) : Boolean {
    if (!game.table.containsKey(key = tileSetId))
        return false

    val tileSet = game.table[tileSetId]!!
    val playedIntersect = tileSet.tiles.filter { tileIsRegular(it) }.intersect(game.turn.tilesIntroduced)
    return if (playedIntersect.size == tileSet.tiles.size)
        false // tileSet contains tiles not played in this move
    else {
        game.table.remove(tileSet.id)
        player.hand[tileSetId] = tileSet
        true
    }
}

fun splitTileSet(move : Move) : Boolean {
    val tileSetId = move.splitSetId
    val index : Int = move.splitIndex

    val location = when (move.moveLocation) {
        MoveLocation.TABLE -> move.game.table
        MoveLocation.HAND -> move.game.getCurrentPlayer().hand
    }

    if (!location.containsKey(key = tileSetId))
        return false

    val newSets = location[tileSetId]?.let { split(tileSet = it, index = index) }
    if (newSets != null && newSets.size != 2) {
        return false
    }

    for (group in newSets!!) {
        location[group.id] = group
        move.game.tileSets[group.id] = group
    }
    location.remove(key = tileSetId)
    move.game.tileSets.remove(key = tileSetId)

    return true
}

fun mergeTileSets(move: Move) : Boolean {
    val location = when (move.moveLocation) {
        MoveLocation.TABLE -> move.game.table
        MoveLocation.HAND -> move.game.getCurrentPlayer().hand
    }
    var newSet = location[move.leftMergeId]?.let { location[move.rightMergeId]?.let { it1 -> merge(left = it, right = it1) } }
    move.game.tileSets.remove(move.leftMergeId)
    move.game.tileSets.remove(move.rightMergeId)
    move.game.tileSets[newSet!!.id] = newSet

    return true
}

fun split(tileSet : TileSet, index : Int) : List<TileSet> {
    if (index == 0 || index == tileSet.tiles.size)
        return listOf(tileSet)
    var head = TileSet(tiles = tileSet.tiles.subList(0, index))
    var tail = TileSet(tiles = tileSet.tiles.subList(index, tileSet.tiles.size))
    return listOf(head, tail)
}

fun merge(left: TileSet, right : TileSet) : TileSet {
    var tiles = mutableListOf<Tile>()
    tiles.addAll(elements = left.tiles)
    tiles.addAll(elements = right.tiles)
    return TileSet(tiles)
}

fun playerEndsTurn(game: Game, player: Player) : Boolean {
    // check if the player played tiles
    if (!playerHasInitialPlay(player = player)
        && !playerHasPlayedInTurn(player = player))
        return false

    if(!hasPlayerWon(game = game, player = player)
        && playerHasPlayedInTurn(player = player)
        && tableIsValid(game = game))
        endTurn(game = game)
    return true
}

fun playerEndsInitialTurn(game: Game) : Boolean {
    return if (tileListValue(game.turn.tilesIntroduced) > 30 && tableIsValid(game = game)) {
        endTurn(game = game)
        true
    } else
        false
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

fun playerCanPlay(move: Move) : Boolean {
    return playerIsInGame(game = move.game, player = move.player)
            && isCurrentPlayer(game = move.game, player = move.player)
}

fun playerCanArrange(move: Move) : Boolean {
    return playerCanArrangeHand(move = move)
            || isCurrentPlayer(game = move.game, player = move.player)
}

fun playerCanArrangeHand(move: Move) : Boolean {
    return playerIsInGame(game = move.game, player = move.player)
            && (move.moveLocation == MoveLocation.HAND)
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
        .reduce { sum, element -> sum && element }
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