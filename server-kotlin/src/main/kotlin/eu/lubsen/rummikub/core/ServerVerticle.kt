package eu.lubsen.rummikub.core

import eu.lubsen.rummikub.idl.client.*
import eu.lubsen.rummikub.idl.server.*
import eu.lubsen.rummikub.model.Lounge
import eu.lubsen.rummikub.model.Player
import eu.lubsen.rummikub.util.Failure
import eu.lubsen.rummikub.util.Success
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import io.vertx.core.net.JksOptions
import io.vertx.core.net.PemKeyCertOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import java.util.*
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger


class ServerVerticle(private val ssl:Boolean) : AbstractVerticle() {
    private var clientSockets = mutableMapOf<UUID, ServerWebSocket>()

    private var lounge = Lounge()

    override fun start(future: Future<Void>) {
        var server = when(ssl) {
            true -> {
                vertx.createHttpServer(
                    HttpServerOptions().setSsl(true).setPemKeyCertOptions(
                        PemKeyCertOptions().setCertPath("/etc/ssl/local/cert.pem").setKeyPath("/etc/ssl/local/key.pem")))
                // /usr/syno/etc/certificate/_archive/
                // https://stackoverflow.com/questions/26028971/docker-container-ssl-certificates
            }
            false -> vertx.createHttpServer()
        };
        var router = Router.router(vertx)
        router.route().handler(BodyHandler.create());

        router.get("/").handler(this::handleRoot)
        router.get("/join").handler(this::joinClient)
        server.requestHandler(router).listen(8080)

        Logger.getAnonymousLogger().log(LogRecord(Level.INFO, "Server running"))
    }

    private fun handleRoot(context: RoutingContext) {
        context.response().putHeader("content-type", "application/json").setStatusCode(200).end(
            JsonObject()
                .put("server-type", "rummikub-server-kotlin")
                .put("health", "running")
                .put("websocket-join-endpoint", "/join").encode()
        )
    }

    private fun joinClient(context: RoutingContext) {
        var name : String = context.request().params().get("name")

        context.request().toWebSocket { ar ->
            if (ar.succeeded() && ar.result() != null) {
                connectHandler(name = name, webSocket = ar.result())
            } else
                context.response().setStatusCode(400).end()
        }
    }

    private fun connectHandler(name: String, webSocket: ServerWebSocket) {
        webSocket.handler(this::receiveMessageHandler)
        webSocket.closeHandler { closeHandler() }

        val player = Player(playerName = name)
        when(playerNameExists(lounge = lounge, name = player.playerName)) {
            true -> {
                webSocket.writeTextMessage(PlayerNameExists(eventNumber = 0, name = player.playerName).toJson())
                webSocket.close()
            }
            false -> {
                clientSockets[player.id] = webSocket
                playerConnects(lounge = lounge, player = player)

                sendMessage(
                    Connected(eventNumber = 0, player = player)
                        .addRecipient(recipient = player.id)
                )
                sendMessage(
                    PlayerConnected(eventNumber = 0, player = player)
                        .addRecipient(recipients = lounge.players.keys)
                )
            }
        }
    }

    private fun closeHandler() {
        var closedPlayerIds = mutableListOf<UUID>();
        clientSockets.forEach { (id, socket) ->
            if (socket.isClosed)
                closedPlayerIds.add(id)
        }
        closedPlayerIds.forEach { playerId ->
            clientSockets.remove(key = playerId)
            var player = lounge.players[playerId]!!

            when (val gameResult = findGameForPlayer(lounge = lounge, player = player)) {
                is Success -> {
                    val game = gameResult.result()
                    game.stopGame()
                    when (val message = playerDisconnects(lounge = lounge, player = player)) {
                        is Success -> {
                            sendMessage(
                                message = message.result()
                                    .addRecipient(recipients = game.players.keys)
                            )
                            sendMessage(
                                message = GameStopped(eventNumber = 0, game = game)
                                    .addRecipient(recipients = game.players.keys)
                            )
                            sendMessage(
                                messages = game.players.values
                                    .map {
                                        GameStateResponse(eventNumber = 0, game = game, player = it)
                                            .addRecipient(recipient = it.id)
                                    }.toList()
                            )

                            when (val cleaned = cleanupGame(lounge = lounge, game = game)) {
                                is Success -> {
                                    sendMessage(message = cleaned.result())
                                }
                                else -> {
                                }
                            }
                        }
                        is Failure -> {
                            sendMessage(
                                MessageResponse(
                                    eventNumber = 0,
                                    message = "Player ${player.playerName} disconnected from your game, but something went wrong in handling it"
                                )
                                    .addRecipient(recipients = game.players.keys)
                            )
                        }
                    }
                }
                is Failure -> {
                } // Player is not currently in a game. Fine.
            }
        }
    }

    private fun receiveMessageHandler(buffer: Buffer) =
        receiveMessageJson(json = JsonObject(buffer.getString(0, buffer.length())))

    private fun receiveMessageJson(json: JsonObject) {
        val message = when(ClientMessageType.valueOf(value = json.getString("messageType"))) {
            ClientMessageType.CreateGame -> CreateGame(json = json)
            ClientMessageType.RemoveGame -> RemoveGame(json = json)
            ClientMessageType.JoinGame -> JoinGame(json = json)
            ClientMessageType.LeaveGame -> LeaveGame(json = json)
            ClientMessageType.StartGame -> StartGame(json = json)
            ClientMessageType.StopGame -> StopGame(json = json)
            ClientMessageType.RequestGameList -> RequestGameList(json = json)
            ClientMessageType.RequestPlayerList -> RequestPlayerList(json = json)
            ClientMessageType.RequestPlayerListForGame -> RequestPlayerListForGame(json = json)
            ClientMessageType.RequestGameState -> RequestGameState(json = json)
            ClientMessageType.PlayerMove -> PlayerMove(json = json, lounge = lounge)
        }

        handleClientMessage(message = message)
    }

    private fun handleClientMessage(message: ClientMessage) {
        if (isValidPlayerId(lounge = lounge, playerId = message.playerId)) {
            val result = when (message) {
                is CreateGame -> handleCreateGame(
                    lounge = lounge,
                    gameName = message.gameName,
                    ownerId = message.playerId
                )
                is RemoveGame -> handleRemoveGame(
                    lounge = lounge,
                    gameName = message.gameName,
                    playerId = message.playerId
                )
                is JoinGame -> handleJoinGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is LeaveGame -> handleLeaveGame(
                    lounge = lounge,
                    gameName = message.gameName,
                    playerId = message.playerId
                )
                is RequestGameList -> handleRequestGameList(lounge = lounge, playerId = message.playerId)
                is RequestPlayerList -> handleRequestPlayerList(lounge = lounge, playerId = message.playerId)
                is RequestPlayerListForGame -> handleRequestPlayerListForGame(
                    lounge = lounge,
                    playerId = message.playerId,
                    gameName = message.gameName
                )
                is RequestGameState -> handleRequestGameState(
                    lounge = lounge,
                    gameName = message.gameName,
                    playerId = message.playerId
                )
                is StartGame -> handleStartGame(
                    lounge = lounge,
                    gameName = message.gameName,
                    playerId = message.playerId
                )
                is StopGame -> handleStopGame(lounge = lounge, gameName = message.gameName, playerId = message.playerId)
                is PlayerMove -> handlePlayerMove(lounge = lounge, gameName = message.gameName, move = message.move)
            }
            when(result) {
                is Success<*> -> {
                    when (result.result()) {
                        is ServerMessage -> {
                            sendMessage(message = result.result() as ServerMessage)
                        }
                        else -> {
                            val messages = result.result() as List<ServerMessage>
                            sendMessage(messages = messages)
                        }
                    }
                }
                is Failure -> sendMessage(
                    message = MessageResponse(
                        eventNumber = 0,
                        message = result.message()
                    ).addRecipient(message.playerId)
                )
            }
        } else
            sendMessage(
                message = MessageResponse(
                    eventNumber = 0,
                    message = "Invalid player ID."
                ).addRecipient(message.playerId)
            )
    }

    private fun sendMessage(messages: List<ServerMessage>) {
        messages.forEach { sendMessage(message = it) }
    }

    private fun sendMessage(message: ServerMessage) {
        for (recipient in message.recipients) {
            val channel = clientSockets[recipient]
            channel?.writeTextMessage(message.toJson())
        }
    }
}