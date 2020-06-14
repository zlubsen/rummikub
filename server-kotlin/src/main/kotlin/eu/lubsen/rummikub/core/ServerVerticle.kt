package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.client.*
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
import kotlin.concurrent.timer

class ServerVerticle : AbstractVerticle() {
    private lateinit var heartbeatTimer : Timer

    private var clientSockets = mutableMapOf<String, ServerWebSocket>()

    private var lounge = Lounge()

    override fun start(future: Future<Void>) {
        var server = vertx.createHttpServer()
        var router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/").handler(this::handleRoot)
        router.get("/join").handler(this::joinClient)
        server.requestHandler(router).listen(8080)

        println("Server running")

        heartbeatTimer = timer("heartbeat",false,0,2000) {
            for ((id, socket) in clientSockets) {
                if(!socket.isClosed) socket.writeTextMessage(System.currentTimeMillis().toString())}
            }
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
        webSocket.handler(this::receiveMessage)
        webSocket.closeHandler {
            var playerId = ""
            clientSockets.forEach {
                    (id, socket) ->
                if (socket == webSocket)
                    playerId = id
            }
            if (!playerId.isNullOrBlank()) {
                clientSockets.remove(playerId)
            }
        }
        clientSockets[userName] = webSocket
    }

    private fun receiveMessage(buffer : Buffer) {
        val json = JsonObject.mapFrom(buffer.getString(0, buffer.length()))
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

        processClientMessage(message)
    }

    private fun processClientMessage(message : ClientMessage) {
        if (isValidPlayerId(lounge = lounge, playerId = message.playerId)) {
            when (message) {
                is CreateGame -> createGame(lounge = lounge, name = message.gameName, ownerId = message.playerId)
                is RemoveGame -> playerIsOwner(lounge = lounge, gameName = message.gameName, playerId = message.playerId) && removeGame(
                    lounge = lounge,
                    gameName = message.gameName,
                    ownerId = message.playerId
                )
                is JoinGame -> isValidGameName(lounge = lounge, gameName = message.gameName) && joinGame(game = lounge.games[message.gameName]!!, player = lounge.players[message.playerId]!!)
                is LeaveGame -> isValidGameName(lounge = lounge, gameName = message.gameName) && leaveGame(game = lounge.games[message.gameName]!!, player = lounge.players[message.playerId]!!)
                is RequestGameList -> respondGameList(requesterId = message.playerId)
                is RequestPlayerList -> respondPlayerList(requesterId = message.playerId)
                is StartGame -> playerIsOwner(lounge = lounge, gameName = message.gameName, playerId = message.playerId) && startGame(game = lounge.games[message.gameName]!!)
                is StopGame -> playerIsOwner(lounge = lounge, gameName = message.gameName, playerId = message.playerId) && stopGame(game = lounge.games[message.gameName]!!)
                is PlayerMove -> isValidGameName(lounge = lounge, gameName = message.gameName) && tryMove(game = lounge.games[message.gameName]!!, move = message.move)
            }
        }
    }

    private fun respondGameList(requesterId : UUID) {
        TODO("Server message: respond list of games")
    }

    private fun respondPlayerList(requesterId : UUID) {
        TODO("Server message: respond list of players")
    }
}