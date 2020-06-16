package eu.lubsen.rummikub.idl.client

import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

internal class ClientMessageTest {
    @Test
    fun clientMessageFromJson() {
        val type = ClientMessageType.CreateGame
        val playerId = UUID.randomUUID()
        val gameName = "testGame"
        val json = """
            {
                "messageType" : "$type",
                "playerId" : "$playerId",
                "gameId" : "$gameName"
            }
        """.trimIndent()

        val message = CreateGame(json = JsonObject(json))
        assertEquals(expected = type, actual = message.type)
        assertEquals(expected = playerId, actual = message.playerId)
        assertEquals(expected = gameName, actual = message.gameName)
    }
}