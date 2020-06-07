package eu.lubsen.rummikub.model

import eu.lubsen.rummikub.core.*
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.UUID

class Game constructor(val name: String) {
    var gameState = GameState.JOINING
    var players : MutableMap<UUID, Player> = mutableMapOf()
    var table : MutableMap<UUID, TileSet> = mutableMapOf()
    var heap : MutableList<Tile> = mutableListOf()

    var currentPlayerIndex : Int = 0

    var tileSets : MutableMap<UUID, TileSet> = mutableMapOf()

    fun addPlayer(player: Player) {
        assert(gameState == GameState.JOINING)
        players[player.id] = player
    }

    fun removePlayer(player: Player) {
        assert(gameState == GameState.JOINING)
        if (players.containsKey(player.id))
            players.remove(player.id)
    }

    fun getCurrentPlayer() : Player {
        return players[players.keys.toList()[currentPlayerIndex]]!!
    }

    fun nextPlayer() {
        if (currentPlayerIndex < players.keys.indices.last)
            currentPlayerIndex++
        else
            currentPlayerIndex = players.keys.indices.first
    }

    fun getPlayer(id : UUID) : Player? {
        return players[id]
    }

    fun tryMove(move: Move) : Boolean {
        assert(gameState == GameState.STARTED)
        val allowedToPlay = playerCanPlay(move)
        val allowedToArrange = playerCanArrangeHand(move)

        return when (move.moveType) {
            MoveType.HAND_TO_TABLE -> allowedToPlay && playerPutsTilesOnTable(move.game, move.player, move.tilesToTable)
            MoveType.SPLIT -> allowedToArrange && splitTileSet(move)
            MoveType.MERGE -> allowedToArrange && mergeTileSets(move)
            MoveType.TAKE_FROM_HEAP -> allowedToPlay && playerDrawsFromHeap(move.game, move.player)
            MoveType.END_TURN -> allowedToPlay && playerEndsTurn(move.game, move.player)
            MoveType.TAKE_JOKER -> allowedToPlay && TODO()
        }
    }

    fun startGame() {
        assert(gameState == GameState.JOINING)
        initGame()
        gameState = GameState.STARTED
    }

    private fun initGame() {
        initHeap(createTiles())
        initPlayerBoards()
    }

    fun createTiles() : List<Tile> {
        val tiles = mutableListOf<Tile>()
        TileColor.values().forEach { color ->
            TileNumber.values().forEach { number ->
                tiles.add(Tile(number, color, TileType.REGULAR))
            }
        }
        tiles.add(Tile(TileNumber.ANY,TileColor.ANY, TileType.JOKER))
        tiles.add(Tile(TileNumber.ANY,TileColor.ANY, TileType.JOKER))

        return tiles
    }

    private fun initHeap(initialTiles : List<Tile>) {
        heap.addAll(initialTiles)
    }

    private fun initPlayerBoards() {
        for (player in players.values) {
            for (i in 1..14) {
                playerDrawsFromHeap(this, player)
            }
        }
    }

    fun stopGame() {
        gameState = GameState.FINISHED
    }

    fun toJson() : JsonObject {
        return JsonObject()
            .put("name", name)
            .put("gameState", gameState.name)
            .put("currentPlayer", getCurrentPlayer().id.toString())
            .put("players", JsonArray(players.values.toList().map { p -> p.toJson() }))
            .put("table", JsonArray(table.values.toList()))
    }
}

