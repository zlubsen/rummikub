package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.client.*
import eu.lubsen.rummikub.idl.server.*
import eu.lubsen.rummikub.model.*
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
                clientSockets.remove(playerId!!)
                lounge.playerDisconnects(lounge.players[playerId!!]!!)
            }
        }
        // add the player
        val player = Player(userName)
        clientSockets[player.id] = webSocket
        lounge.players[player.id] = player

        val response = Connected(0, player)
        clientSockets[player.id]!!.writeTextMessage(response.toJson())
    }

    private fun receiveMessageHandler(buffer : Buffer) =
        receiveMessageJson(JsonObject(buffer.getString(0, buffer.length())))

    private fun receiveMessageJson(json : JsonObject) {
        val message = when(ClientMessageType.valueOf(json.getString("messageType"))) {
            ClientMessageType.CreateGame -> CreateGame(json)
            ClientMessageType.RemoveGame -> RemoveGame(json)
            ClientMessageType.JoinGame -> JoinGame(json)
            ClientMessageType.LeaveGame -> LeaveGame(json)
            ClientMessageType.StartGame -> StartGame(json)
            ClientMessageType.StopGame -> StopGame(json)
            ClientMessageType.RequestGameList -> RequestGameList(json)
            ClientMessageType.RequestPlayerList -> RequestPlayerList(json)
            ClientMessageType.PlayerMove -> PlayerMove(json, lounge)
        }

        handleClientMessage(message)
    }

    private fun handleClientMessage(message : ClientMessage) {
        if (isValidPlayerId(lounge = lounge, playerId = message.playerId)) {
            val result = when (message) {
                is CreateGame -> handleCreateGame(lounge = lounge, gameName = message.gameName, ownerId = message.playerId)
                is RemoveGame -> handleRemoveGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is JoinGame -> handleJoinGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is LeaveGame -> handleLeaveGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is RequestGameList -> respondGameList(requesterId = message.playerId)
                is RequestPlayerList -> respondPlayerList(requesterId = message.playerId)
                is StartGame -> handleStartGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is StopGame -> handleStopGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is PlayerMove -> handlePlayerMove(lounge = lounge, gameName = message.gameName, move = message.move)
            }
            // TODO: figure out how to collect recipients and the message; as a Pair in a Result, or in the ServerMessage object
            when(result) {
                is Success<*> -> {  }
                is Failure -> respondMessage(
                    recipients = listOf(message.playerId),
                    message = MessageResponse(eventNumber = 0, message = result.message()))
            }
        } else
            respondMessage(
                recipients = listOf(message.playerId),
                message = MessageResponse(eventNumber = 0, message = "Invalid player ID."))
    }

    private fun respondMessage(recipients : List<UUID>, message : ServerMessage) {
        for (recipient in recipients) {
            val channel = clientSockets[recipient]
            channel?.writeTextMessage(message.toJson())
        }
    }

    private fun respondGameList(requesterId : UUID) : Result {
        val message = GameListResponse(eventNumber = 0, games = lounge.listGames())

        val receiver = clientSockets[requesterId]
        receiver?.writeTextMessage(message.toJson())
        return Success(true)
    }

    private fun respondPlayerList(requesterId : UUID) {
        val message = PlayerListResponse(eventNumber = 0, players = lounge.listPlayers())

        val receiver = clientSockets[requesterId]
        receiver?.writeTextMessage(message.toJson())
    }
}