package eu.lubsen.rummikub.core

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
        var message = JsonObject.mapFrom(buffer.getString(0, buffer.length()))
        println(message)
    }

    fun parseMove(json : JsonObject) : Move {
        val game : Game = lounge.games[json.getString("gameName")]!!
        val player : Player = lounge.players[UUID.fromString(json.getString("playerId"))]!!
        val moveType : MoveType = MoveType.valueOf(json.getString("moveType"))!!
        var move = Move(game, player, moveType)

        when (move.moveType) {
            MoveType.HAND_TO_TABLE -> {move.tilesToTable = UUID.fromString(json.getString("tileGroupId"))}
            MoveType.SPLIT -> {
                move.moveLocation = MoveLocation.valueOf(json.getString("moveLocation"))
                move.splitIndex = json.getInteger("splitIndex")
                move.splitGroupId = UUID.fromString(json.getString("splitGroupId"))
            }
            MoveType.MERGE -> {
                move.moveLocation = MoveLocation.valueOf(json.getString("moveLocation"))
                move.leftMergeId = UUID.fromString(json.getString("leftMergeId"))
                move.rightMergeId = UUID.fromString(json.getString("rightMergeId"))
            }
            MoveType.TAKE_FROM_HEAP -> null
            MoveType.END_TURN -> null
            MoveType.TAKE_JOKER -> TODO() //
        }
        return move
    }
}