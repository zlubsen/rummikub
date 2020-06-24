package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.client.*
import eu.lubsen.rummikub.idl.server.*
import eu.lubsen.rummikub.model.*
import eu.lubsen.rummikub.util.Failure
import eu.lubsen.rummikub.util.Success
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import java.util.*

class ServerVerticle : AbstractVerticle() {
//    private lateinit var heartbeatTimer : Timer

    private var clientSockets = mutableMapOf<UUID, ServerWebSocket>()

    private var lounge = Lounge()

    override fun start(future: Future<Void>) {
        var server = vertx.createHttpServer()
        var router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/").handler(this::handleRoot)
        router.get("/join").handler(this::joinClient)
        server.requestHandler(router).listen(8080)

        println("Server running")

//        heartbeatTimer = timer("heartbeat",false,0,2000) {
//            for ((id, socket) in clientSockets) {
//                if(!socket.isClosed) socket.writeTextMessage(System.currentTimeMillis().toString())}
//            }
    }

    private fun handleRoot(context: RoutingContext) {
        context.response().putHeader("content-type", "application/json").setStatusCode(200).end(
            JsonObject()
                .put("server-type","rummikub-server-kotlin")
                .put("health", "running")
                .put("web-socket-join-endpoint", "/").encode())
    }

    private fun joinClient(context: RoutingContext) {
        var userName : String = context.request().params().get("name")

        var webSocket = context.request().upgrade()
        webSocket.handler(this::receiveMessageHandler)
        webSocket.closeHandler {
            var playerId : UUID? = null
            clientSockets.forEach {
                    (id, socket) ->
                if (socket.isClosed)
                    playerId = id
            }
            if (playerId != null) {
                clientSockets.remove(key = playerId!!)
                playerDisconnects(lounge = lounge, player = lounge.players[playerId!!]!!)
            }
        }
        // add the player
        val player = Player(playerName = userName)
        clientSockets[player.id] = webSocket
        playerConnects(lounge = lounge, player = player)

        sendMessage(
            Connected(eventNumber = 0, player = player)
                .addRecipient(recipient = player.id))
        sendMessage(
            PlayerConnected(eventNumber = 0, player = player)
                .addRecipient(recipients = lounge.players.keys)
        )
    }

    private fun receiveMessageHandler(buffer : Buffer) =
        receiveMessageJson(json = JsonObject(buffer.getString(0, buffer.length())))

    private fun receiveMessageJson(json : JsonObject) {
        val message = when(ClientMessageType.valueOf(value = json.getString("messageType"))) {
            ClientMessageType.CreateGame -> CreateGame(json = json)
            ClientMessageType.RemoveGame -> RemoveGame(json = json)
            ClientMessageType.JoinGame -> JoinGame(json = json)
            ClientMessageType.LeaveGame -> LeaveGame(json = json)
            ClientMessageType.StartGame -> StartGame(json = json)
            ClientMessageType.StopGame -> StopGame(json = json)
            ClientMessageType.RequestGameList -> RequestGameList(json = json)
            ClientMessageType.RequestPlayerList -> RequestPlayerList(json = json)
            ClientMessageType.PlayerMove -> PlayerMove(json = json, lounge = lounge)
        }

        handleClientMessage(message = message)
    }

    private fun handleClientMessage(message : ClientMessage) {
        if (isValidPlayerId(lounge = lounge, playerId = message.playerId)) {
            val result = when (message) {
                is CreateGame -> handleCreateGame(lounge = lounge, gameName = message.gameName, ownerId = message.playerId)
                is RemoveGame -> handleRemoveGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is JoinGame -> handleJoinGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is LeaveGame -> handleLeaveGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is RequestGameList -> handleRequestGameList(lounge = lounge, playerId = message.playerId)
                is RequestPlayerList -> handleRequestPlayerList(lounge = lounge, playerId = message.playerId)
                is StartGame -> handleStartGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is StopGame -> handleStopGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is PlayerMove -> handlePlayerMove(lounge = lounge, gameName = message.gameName, move = message.move)
            }
            when(result) {
                is Success<ServerMessage> -> { sendMessage(message = result.result()) }
                is Failure<*> -> sendMessage(
                        message = MessageResponse(
                            eventNumber = 0,
                            message = result.message()
                        ).addRecipient(message.playerId))
            }
        } else
            sendMessage(
                message = MessageResponse(
                    eventNumber = 0,
                    message = "Invalid player ID.").addRecipient(message.playerId))
    }

    private fun sendMessage(message : ServerMessage) {
        for (recipient in message.recipients) {
            val channel = clientSockets[recipient]
            channel?.writeTextMessage(message.toJson())
        }
    }
}