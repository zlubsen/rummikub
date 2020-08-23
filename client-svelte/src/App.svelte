<script>
    import Tailwindcss from './Tailwindcss.svelte';
    import {Connection} from "./utils/Connection.js";
    import {LOCATION_TABLE, LOCATION_HAND} from "./utils/GameUtils.js";
    import * as IDL from "./idl/ClientMessages.js";
    import RegisterPlayer from "./components/RegisterPlayer.svelte";
    import GameBoard from "./components/GameBoard.svelte";
    import GameList from "./components/GameList.svelte";
    import PlayerHand from "./components/PlayerHand.svelte";
    import TurnControls from "./components/TurnControls.svelte";
    import {log} from "./stores/logMessage.js";
    import {onDestroy} from "svelte";

    const updateInterval = 120000; // every 2 minutes
    const serverAddress = "ws://192.168.8.158:8080/join"

    let playerId = undefined;
    let playerName = undefined;
    let currentGame = undefined;

    let connection;
    let requestGamesInterval;
    let requestPlayersInterval;
    let logMessage;

    let players = new Map();
    let games = new Map();
    let playersInSelectedGame = [];
    let playersInCurrentGame = [];

    let currentPlayer = undefined;
    $: gameState = games.has(currentGame) ? games.get(currentGame).gameState : null;
    $: isPlayersTurn = currentPlayer !== undefined && currentPlayer === playerId;
    let table = new Map();
    let hand = new Map();

    const testData = false;
    if(testData) setTestData();

    const unsubscribeLogging = log.subscribe(value => {
        logMessage = value;
    });

    onDestroy(unsubscribeLogging);

    const receiveHandler = function (message) {
        let messageType = message.messageType
        // console.log("received msg: " + message.messageType);
        switch (messageType) {
            case "MessageResponse":
                logMessage = message.message;
                break;
            case "Connected":
                playerId = message.player.id
                playerName = message.player.name
                connection.sendJson(IDL.msgRequestPlayerList(playerId));
                connection.sendJson(IDL.msgRequestGameList(playerId));
                requestGamesInterval = setInterval(() => connection.sendJson(IDL.msgRequestGameList(playerId)), updateInterval);
                requestPlayersInterval = setInterval(() => connection.sendJson(IDL.msgRequestPlayerList(playerId)), updateInterval);
                logMessage = "Welcome to Rummikub!";
                break;
            case "PlayerConnected":
                players.set(message.player.id, message.player.name);
                players = players;
                logMessage = "Player joined: " + message.player.name;
                break;
            case "PlayerDisconnected":
                players.delete(message.player.id);
                players = players;
                logMessage = "Player disconnected: " + message.player.name;
                break;
            case "GameCreated":
                games.set(message.game.gameName, message.game);
                games = games;
                if (message.game.owner === playerId)
                    connection.sendJson(IDL.msgJoinGame(playerId, message.game.gameName));
                logMessage = "Game " + message.game.gameName + " created by " + players.get(message.game.owner);
                break;
            case "GameRemoved":
                if (currentGame === message.gameName) {
                    clearGame();
                }
                games.delete(message.gameName);
                games = games;
                logMessage = "Game " + message.gameName + " removed";
                break;
            case "PlayerJoinedGame":
                if (message.playerId === playerId) {
                    currentGame = message.gameName;
                }
                playersInCurrentGame.push(message.playerId);
                playersInCurrentGame = playersInCurrentGame;
                logMessage = "Player " + players.get(message.playerId) + " joined game " + message.gameName;
                break;
            case "PlayerLeftGame":
                if (message.playerId === playerId)
                    clearGame();
                playersInCurrentGame.splice(playersInCurrentGame.indexOf(message.playerId), 1);
                playersInCurrentGame = playersInCurrentGame
                logMessage = "Player " + players.get(message.playerId) + " left game " + message.gameName;
                break;
            case "GameStarted":
                if (games.has(message.gameName))
                    games.get(message.gameName).gameState = message.gameState;
                if (currentGame !== undefined && currentGame === message.gameName)
                    connection.sendJson(IDL.msgRequestGameState(playerId, currentGame));
                logMessage = "Game " + currentGame + " started";
                break;
            case "GameStopped":
                if (games.has(message.gameName)) {
                    games.get(message.gameName).gameState = message.gameState;
                    games = games;
                }
                if (currentGame === message.gameName) {
                    // TODO cleanup this game
                    // games.delete(message.gameName);
                    // games = games
                }
                logMessage = "Game " + message.gameName + " was stopped."
                break;
            case "GameFinished":
                logMessage = players.get(message.winner) + " wins the game!";
                if (games.has(message.gameName)) {
                    games.get(message.gameName).gameState = message.gameState;
                    games = games;
                }
                // TODO design mechanism to return to lounge with no current game, clean up finished game
                // - owner removes the game
                // - other players leave the game
                break;
            case "PlayedTilesHandToTable":
                if (hand.has(message.tileSet.id)) {
                    hand.delete(message.tileSet.id);
                    table.set(message.tileSet.id, message.tileSet);
                    hand = hand;
                    table = table;
                }
                break;
            case "PlayedTilesTableToHand":
                if (table.has(message.tileSet.id)) {
                    table.delete(message.tileSet.id);
                    hand.set(message.tileSet.id, message.tileSet);
                    hand = hand;
                    table = table;
                }
                break;
            case "TableChangedHandToTable":
                table.set(message.tileSet.id, message.tileSet);
                table = table;
                break;
            case "TableChangedTableToHand":
                if (table.has(message.tileSet.id)) {
                    table.delete(message.tileSet.id);
                    table = table;
                }
                break;
            case "PlayedTurnEnded":
                currentPlayer = message.nextPlayerId;
                logMessage = "Turn ended. Next up is " + players.get(currentPlayer) + ".";
                break;
            case "PlayedTookFromHeap":
                hand.set(message.tileSet.id, message.tileSet);
                hand = hand;
                break;
            case "PlayerTookFromHeap":
                logMessage = players.get(message.playerId) + " took a tile from the heap.";
                break;
            case "PlayedTileSetSplit":
                if (message.location === "HAND") {
                    hand.delete(message.originalId);
                    hand.set(message.leftSet.id, message.leftSet);
                    hand.set(message.rightSet.id, message.rightSet);
                    hand = hand;
                } else if (message.location === "TABLE") {
                    table.delete(message.originalId);
                    table.set(message.leftSet.id, message.leftSet);
                    table.set(message.rightSet.id, message.rightSet);
                    table = table;
                }
                break;
            case "PlayedTileSetsMerged":
                if (message.location === "HAND") {
                    hand.delete(message.sourceId);
                    hand.delete(message.targetId);
                    hand.set(message.tileSet.id, message.tileSet);
                    hand = hand;
                } else if (message.location === "TABLE") {
                    table.delete(message.sourceId);
                    table.delete(message.targetId);
                    table.set(message.tileSet.id, message.tileSet);
                    table = table;
                }
                break;
            case "GameListResponse":
                games.clear();
                message.games.forEach(function (game) {
                    games.set(game.gameName, game);
                });
                games = games;
                break;
            case "PlayerListResponse":
                players.clear();
                message.players.forEach(function (player) {
                    players.set(player.id, player.name);
                });
                players = players;
                break;
            case "PlayerListForGameResponse":
                playersInSelectedGame = []
                message.players.forEach(function (player) {
                    playersInSelectedGame.push(player);
                });
                break;
            case "GameStateResponse":
                hand.clear();
                message.hand.forEach((tileSet) => {
                    hand.set(tileSet.id, tileSet)
                });
                hand = hand;

                table.clear();
                message.table.forEach((tileSet) => {
                    table.set(tileSet.id, tileSet)
                });
                table = table;

                currentPlayer = message.currentPlayer;
                playersInCurrentGame = [];
                message.players.forEach((player) => {
                    playersInCurrentGame.push(player.id);
                });
                playersInCurrentGame = playersInCurrentGame;
                if (games.has(message.game.gameName)) {
                    games.delete(message.game.gameName)
                }
                games.set(message.game.gameName, message.game);
                games = games;
                break;
            default:
                console.log("Unknown message received from server: " + message.messageType);
                break;
        }
    }

    const closeHandler = function() {
        logMessage = "You were disconnected from the server.";
        clearState();
        // TODO cleanup client state
    }

    // window.onbeforeunload = function() {
    //     console.log("beforeunload");
    //     return true;
    // };
    // window.addEventListener('beforeunload', (event) => {
    //     console.log("preventing navigating away...");
    //     // Cancel the event as stated by the standard.
    //     event.preventDefault();
    //     // Chrome requires returnValue to be set.
    //     event.returnValue = 'If you navigate away, you close the session...';
    // });

    function clearState() {
        playerId = undefined;
        playerName = undefined;
        players.clear();
        games.clear();

        clearInterval(requestGamesInterval);
        clearInterval(requestPlayersInterval);

        clearGame();
    }

    function clearGame() {
        currentGame = undefined;
        currentPlayer = undefined;
        playersInCurrentGame = [];

        table.clear()
        hand.clear();
    }

    function eventJoin(event) {
        connection = new Connection(serverAddress, event.detail.playerName, receiveHandler, closeHandler);
    }

    function eventLeave(event) {
        connection.disconnect();
        clearState();
    }

    function eventJoinGame(event) {
        connection.sendJson(IDL.msgJoinGame(playerId, event.detail.gameName));
    }

    function eventLeaveGame(event) {
        connection.sendJson(IDL.msgLeaveGame(playerId, event.detail.gameName));
    }

    function eventCreateGame(event) {
        connection.sendJson(IDL.msgCreateGame(playerId, event.detail.gameName));
    }

    function eventRemoveGame(event) {
        connection.sendJson(IDL.msgRemoveGame(playerId, event.detail.gameName));
    }

    function eventStartGame(event) {
        connection.sendJson(IDL.msgStartGame(playerId, event.detail.gameName));
    }

    function eventStopGame(event) {
        connection.sendJson(IDL.msgStopGame(playerId, event.detail.gameName));
    }

    function eventMerge(event) {
        const msg = IDL.msgMerge(playerId, currentGame, event.detail.sourceId, event.detail.targetId, event.detail.index, event.detail.location);
        connection.sendJson(msg);
    }

    function eventSplit(event) {
        const msg = IDL.msgSplit(playerId, currentGame, event.detail.id, event.detail.index, event.detail.location);
        connection.sendJson(msg);
    }

    function eventMoveTiles(event) {
        if (event.detail.targetLocation === LOCATION_TABLE) {
            connection.sendJson(IDL.msgHandToTable(playerId, currentGame, event.detail.id));
        } else if (event.detail.targetLocation === LOCATION_HAND) {
            connection.sendJson(IDL.msgTableToHand(playerId, currentGame, event.detail.id));
        }
    }

    function eventEndTurn(event) {
        connection.sendJson(IDL.msgEndTurn(playerId, currentGame));
    }

    function eventResetTurn(event) {
        connection.sendJson(IDL.msgResetTurn(playerId, currentGame));
    }

    function eventTakeFromHeap(event) {
        connection.sendJson(IDL.msgTakeFromHeap(playerId, currentGame));
    }

    function clickRequestGames(event) {
        connection.sendJson(IDL.msgRequestGameList(playerId));
    }

    function clickRequestPlayers(event) {
        connection.sendJson(IDL.msgRequestPlayerList(playerId));
    }

    function eventRequestPlayersInGameList(event) {
        connection.sendJson(IDL.msgRequestPlayerListForGame(playerId, event.detail.gameName));
    }

    function setTestData() {
        games.set("Aap",
            {
                gameName: "Aap"
            });
        games.set("Noot",
            {
                gameName: "Noot"
            });
        games.set("Mies",
            {
                gameName: "Mies"
            });

        table.set("xxsdf1",
            {
                id: "xxsdf1",
                tiles: [
                    {color: "BLACK", number: "8", isJoker: false},
                    {color: "BLACK", number: "9", isJoker: false},
                    {color: "BLACK", number: "10", isJoker: false}
                ],
                isValid: true
            });
        table.set("ldjfb5",
            {
                id: "ldjfb5",
                tiles: [
                    {color: "BLUE", number: "1", isJoker: false},
                    {color: "YELLOW", number: "1", isJoker: false},
                    {color: "BLACK", number: "1", isJoker: false}
                ],
                isValid: true
            });
        table.set("sdlkg4",
            {
                id: "sdlkg4",
                tiles: [
                    {color: "BLUE", number: "8", isJoker: false},
                    {color: "BLACK", number: "3", isJoker: false},
                    {color: "RED", number: "10", isJoker: false},
                    {color: "RED", number: "10", isJoker: true}
                ],
                isValid: false
            });
        table.set("livnr5",
            {
                id: "livnr5",
                tiles: [
                    {color: "BLUE", number: "6", isJoker: false},
                    {color: "BLUE", number: "7", isJoker: false},
                    {color: "BLUE", number: "8", isJoker: false},
                    {color: "BLUE", number: "9", isJoker: false},
                    {color: "BLUE", number: "10", isJoker: false},
                    {color: "BLUE", number: "11", isJoker: false},
                    {color: "BLUE", number: "12", isJoker: false},
                    {color: "BLUE", number: "13", isJoker: false},
                ],
                isValid: true
            });

        hand.set("xxsdf1",
            {
                id: "xxsdf1",
                tiles: [
                    {color: "BLACK", number: "8", isJoker: false},
                    {color: "BLACK", number: "9", isJoker: false},
                    {color: "BLACK", number: "10", isJoker: false}
                ],
                isValid: true
            });
        hand.set("ldjfb5",
            {
                id: "ldjfb5",
                tiles: [
                    {color: "BLUE", number: "1", isJoker: false},
                    {color: "YELLOW", number: "1", isJoker: false},
                    {color: "BLACK", number: "1", isJoker: false}
                ],
                isValid: true
            });
        hand.set("sdlkg4",
            {
                id: "sdlkg4",
                tiles: [
                    {color: "BLUE", number: "8", isJoker: false},
                    {color: "BLACK", number: "3", isJoker: false},
                    {color: "RED", number: "10", isJoker: false},
                    {color: "RED", number: "10", isJoker: true}
                ],
                isValid: false
            });
        hand.set("livnr5",
            {
                id: "livnr5",
                tiles: [
                    {color: "BLUE", number: "6", isJoker: false},
                    {color: "BLUE", number: "7", isJoker: false},
                    {color: "BLUE", number: "8", isJoker: false},
                    {color: "BLUE", number: "9", isJoker: false},
                    {color: "BLUE", number: "10", isJoker: false},
                    {color: "BLUE", number: "11", isJoker: false},
                    {color: "BLUE", number: "12", isJoker: false},
                    {color: "BLUE", number: "13", isJoker: false},
                ],
                isValid: true
            });
    }

</script>

<div id="fullscreen-container" class="full-screen-app app-layout w-screen bg-gray-900">
    <header id="header" class="header-area h-16 bg-blue-600 p-3">
        <span class="font-inter text-2xl text-white">Welcome to Rummikub!</span>
        {#if playerName}
            <span class="pl-2 pr-2 font-inter text-white">Player: {playerName}</span>
        {/if}
        {#if currentGame}
            <span class="pl-2 pr-2 font-inter text-white">Game: {currentGame}</span>
            {#each playersInCurrentGame as playerId}
                <span class:bg-orange-500={playerId===currentPlayer}>{players.get(playerId)}</span>
            {/each}
        {/if}
        <RegisterPlayer playerId="{playerId}" on:connect={eventJoin} on:disconnect={eventLeave}/>
    </header>
    <div class="board-area m-1">
        <GameBoard table="{table}" isPlayersTurn="{isPlayersTurn}"
            on:merge={eventMerge}
            on:split={eventSplit}
            on:moveTiles="{eventMoveTiles}"
        />
    </div>
    <div class="game-area m-1">
        <GameList games="{games}" currentGame="{currentGame}" playerId="{playerId}" playersInSelectedGame="{playersInSelectedGame}"
            on:gameDetails={eventRequestPlayersInGameList}
            on:joinGame={eventJoinGame} on:leaveGame={eventLeaveGame}
            on:createGame={eventCreateGame}
            on:removeGame={eventRemoveGame}
            on:startGame={eventStartGame}
            on:stopGame={eventStopGame}
        />
    </div>
    <div class="hand-area h-48 m-1">
        <PlayerHand hand="{hand}"
            on:merge={eventMerge}
            on:split={eventSplit}
            on:moveTiles={eventMoveTiles}
        />
    </div>
    <div class="control-area m-1">
        <TurnControls {gameState} {isPlayersTurn}
            on:endTurn={eventEndTurn}
            on:resetTurn={eventResetTurn}
            on:takeFromHeap={eventTakeFromHeap}
        />
    </div>
    <footer id="footer" class="footer-area h-10 bg-blue-600 p-2">
        <span class="font-inter text-white">
            {#if logMessage}
                {logMessage}
            {:else}
                Enter your name to start playing Rummikub
            {/if}
        </span>
    </footer>
</div>

<style>
    .full-screen-app {
        @apply h-screen w-screen;
        min-height: -webkit-fill-available;
    }
    .app-layout {
        @apply grid;
        grid-template-areas:    'header header'
                                'board game'
                                'hand control'
                                'footer footer';
        grid-template-columns: 5fr minmax(auto, 15em);
        grid-template-rows: auto 1fr auto;
    }
    .header-area {
        grid-area: header;
    }
    .footer-area {
        grid-area: footer;
    }
    .board-area {
        grid-area: board;
    }
    .hand-area {
        grid-area: hand;
    }
    .game-area {
        grid-area: game;
    }
    .control-area {
        grid-area: control;
    }
</style>

<!-- TODO show/hide buttons and such based on gamestate -->
<!-- - can we merge with not owned tilesets during initial play?-->

<!--UX:-->
<!-- - directly move tileset location and merge with other tileset (e.g., directly append a tile to a set on the table)-->
<!-- - hide merge zones in different location (or add move to directly move and merge)-->
<!-- - prevent page from navigating away-->
<!-- - handle when a player disconnects/leaves an ongoing game (show message, cancel the game...)-->

<!-- - cannot take back a played joker in the same turn; because a joker is not counted as part of the played tiles list...-->

<!-- - send gamestate containing ‘boardIsValid’ and ‘hasPlayedInTurn’ values, to enable/disable the End Turn button.-->
<!-- - after player 1 wins, and removes the game, other players in the game get back in the lounge, but still see the previous game (should get an update of the gameslist / remove the finished game). Message ‘Game xxx removed’ is displayed.-->

<!-- - ongoing game shows up twice in the gamelist >> possible due to GameStateResponse -->

<!-- - during initial play: create separate messages for when table is valid/invalid and when player did/did not meet initial play value-->
<!-- - add move: reset/undo all mutations in this turn (or even more eleborate: add undo button for single action)-->
<!-- - after a game is won, the owner can press ‘Start game’ again. Either remove, or restart the game with the same players-->