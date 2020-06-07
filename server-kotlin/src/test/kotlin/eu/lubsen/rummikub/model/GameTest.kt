package eu.lubsen.rummikub.model

internal class GameTest {

    @org.junit.jupiter.api.Test
    fun addPlayer() {
        val game = Game("MyTestGame")
        val player = Player("Player1")

        assert(game.players.isEmpty())

        game.addPlayer(player)

        assert(game.players.isNotEmpty())
        assert(game.players[player.id] == player)
    }

    @org.junit.jupiter.api.Test
    fun removePlayer() {
        val game = Game("MyTestGame")
        val player = Player("Player1")
        game.addPlayer(player)
        game.removePlayer(player)

        assert(game.players.isEmpty())
    }

    @org.junit.jupiter.api.Test
    fun getPlayer() {
        val game = Game("MyTestGame")
        val player = Player("Player1")
        game.addPlayer(player)
        assert(game.getPlayer(player.id) == player)
    }

    @org.junit.jupiter.api.Test
    fun startGame() {
        val game = Game("MyTestGame")
        val player1 = Player("Player1")
        val player2 = Player("Player2")
        game.addPlayer(player1)
        game.addPlayer(player2)
        game.startGame()

        assert(game.heap.size == ((4 * 13) + 2 - (2 * 14)))
        assert(player1.hand.size == 14)
        assert(player2.hand.size == 14)
    }

    @org.junit.jupiter.api.Test
    fun createTiles() {
        val game = Game("MyTestGame")
        val tiles = game.createTiles()
        assert(tiles.size == (4 * 13) + 2)
    }
}