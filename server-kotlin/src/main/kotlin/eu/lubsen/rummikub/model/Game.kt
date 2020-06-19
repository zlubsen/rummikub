package eu.lubsen.rummikub.model

import eu.lubsen.rummikub.core.*
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.UUID

class Game constructor(val name: String, val owner : Player) {
    var gameState = GameState.JOINING
    var players : MutableMap<UUID, Player> = mutableMapOf()
    var table : MutableMap<UUID, TileSet> = mutableMapOf()
    var heap : MutableList<Tile> = mutableListOf()

    var currentPlayerIndex : Int = 0
    lateinit var turn : Turn

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

    fun startGame() {
        assert(gameState == GameState.JOINING)
        initGame()
        setTurn()
        gameState = GameState.STARTED
    }

    private fun initGame() {
        initHeap(createTiles())
        initPlayerBoards()
    }

    fun createTiles() : List<Tile> {
        val tiles = mutableListOf<Tile>()
        TileColor.values().filter { color -> color != TileColor.ANY }.forEach { color ->
            TileNumber.values().filter { number -> number != TileNumber.ANY }.forEach { number ->
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

    fun setTurn() {
        turn = Turn(table = table.values.toList(), playerHand = getCurrentPlayer().hand.values.toList())
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

