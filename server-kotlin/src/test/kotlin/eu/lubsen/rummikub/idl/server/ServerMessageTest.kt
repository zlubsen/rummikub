package eu.lubsen.rummikub.idl.server

import eu.lubsen.rummikub.model.Game
import eu.lubsen.rummikub.model.Player
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ServerMessageTest {
    @Test
    fun playerConnectedToJson() {
        val player = Player("tester")
        val message = PlayerConnected(0, player)
        val json = """
            {
                "messageType" : "PlayerConnected",
                "eventNumber" : 0,
                "player" : {
                    "id" : "${player.id}",
                    "name" : "tester"
                }
            }
        """

        assertEquals(expected = JsonObject(json), actual = JsonObject(message.toJson()))
    }

    @Test
    fun playerDisconnectedToJson() {
        val player = Player("tester")
        val message = PlayerDisconnected(0, player)
        val json = """
            {
                "messageType" : "PlayerDisconnected",
                "eventNumber" : 0,
                "player" : {
                    "id" : "${player.id}",
                    "name" : "tester"
                }
            }
        """

        assertEquals(expected = JsonObject(json), actual = JsonObject(message.toJson()))
    }


    @Test
    fun gameCreatedToJson() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("OurGame", player1)
        game.addPlayer(player1)
        game.addPlayer(player2)
        val message = GameCreated(0, game)

        val json = """
            {
                "messageType" : "GameCreated",
                "eventNumber" : 0,
                "game" : {
                    "name" : "OurGame",
                    "gameState" : "JOINING",
                    "players" : [
                        "${player1.id}",
                        "${player2.id}"
                    ]
                }
            }
        """

        assertEquals(expected = JsonObject(json), actual = JsonObject(message.toJson()))
    }

    @Test
    fun gameRemovedToJson() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game = Game("OurGame", player1)
        game.addPlayer(player1)
        game.addPlayer(player2)
        val message = GameRemoved(0, game)

        val json = """
            {
                "messageType" : "GameRemoved",
                "eventNumber" : 0,
                "gameName" : "OurGame"
            }
        """

        assertEquals(expected = JsonObject(json), actual = JsonObject(message.toJson()))
    }

    @Test
    fun playerJoinedGameToJson() {
        val player1 = Player("tester1")
        val game = Game("OurGame", player1)
        game.addPlayer(player1)
        val message = PlayerJoinedGame(0, game, player1)

        val json = """
            {
                "messageType" : "PlayerJoinedGame",
                "eventNumber" : 0,
                "gameName" : "OurGame",
                "playerId" : "${player1.id}"
            }
        """

        assertEquals(expected = JsonObject(json), actual = JsonObject(message.toJson()))
    }

    @Test
    fun playerLeftGameToJson() {
        val player1 = Player("tester1")
        val game = Game("OurGame", player1)
        game.addPlayer(player1)
        val message = PlayerLeftGame(0, game, player1)

        val json = """
            {
                "messageType" : "PlayerLeftGame",
                "eventNumber" : 0,
                "gameName" : "OurGame",
                "playerId" : "${player1.id}"
            }
        """

        assertEquals(expected = JsonObject(json), actual = JsonObject(message.toJson()))
    }

//    GameStarted
//    @Test
//    fun clientMessageFromJson() {
//
//    }
//    GameStopped
//    @Test
//    fun clientMessageFromJson() {
//
//    }
//    GameFinished
//    @Test
//    fun clientMessageFromJson() {
//
//    }
//    PlayedMove
//    @Test
//    fun clientMessageFromJson() {
//
//    }
//    MessageResponse
//    @Test
//    fun clientMessageFromJson() {
//
//    }


    @Test
    fun gameListResponseToJson() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val game1 = Game("game1", player1)
        val game2 = Game("game2", player2)
        val games = listOf(game1, game2)

        val message = GameListResponse(0, games)

        val json = """
            {
                "messageType" : "GameListResponse",
                "eventNumber" : 0,
                "games" : [
                    { "gameName" : "game1" },
                    { "gameName" : "game2" }
                ]
            }
        """

        assertEquals(expected = JsonObject(json), actual = JsonObject(message.toJson()))
    }

    @Test
    fun playerListResponseToJson() {
        val player1 = Player("tester1")
        val player2 = Player("tester2")
        val players = listOf(player1, player2)

        val message = PlayerListResponse(0, players)

        val json = """
            {
                "messageType" : "PlayerListResponse",
                "eventNumber" : 0,
                "players" : [
                    { "id" : "${player1.id}", "name" : "tester1" },
                    { "id" : "${player2.id}", "name" : "tester2" }
                ]
            }
        """

        assertEquals(expected = JsonObject(json), actual = JsonObject(message.toJson()))
    }
}