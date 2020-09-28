<script>
    import Tailwindcss from './Tailwindcss.svelte';
    import {Connection} from "./utils/Connection.js";
    import {LOCATION_TABLE, LOCATION_HAND} from "./utils/GameUtils.js";
    import * as IDL from "./idl/ClientMessages.js";
    import RegisterPlayer from "./components/RegisterPlayer.svelte";
    import GameList from "./components/GameList.svelte";
    import TurnControls from "./components/TurnControls.svelte";
    import Modal from "./components/utils/Modal.svelte";
    import {log} from "./stores/logMessage.js";
    import {onDestroy} from "svelte";
    import TileArea from "./components/TileArea.svelte";
    import SlidingSidebar from "./components/utils/SlidingSidebar.svelte";

    const updateInterval = 120000; // every 2 minutes
    const serverAddress = __config.env.SERVER_URL;

    let player = undefined;
    let currentGame = undefined;

    let connection;
    let requestGamesInterval;
    let requestPlayersInterval;
    const maxNoOfLogMessages = 20;
    let logMessage = ["Enter your name to start playing Rummikub"];

    let players = new Map();
    let games = new Map();
    let playersInSelectedGame = [];
    let playersInCurrentGame = [];

    let currentPlayer = undefined;
    $: gameState = games.has(currentGame) ? games.get(currentGame).gameState : null;
    let turnState = undefined;
    $: isPlayersTurn = currentPlayer !== undefined && currentPlayer === player.id;
    $: if (isPlayersTurn) {
        turnState = setTurnState(false, false, false);
        console.log("turnState is " + turnState);
    } else {
        turnState = undefined;
        console.log("turnState is cleared.");
    }
    let table = new Map();
    let hand = new Map();

    function setTurnState(tableValid, playedInTurn, initialPlay) {
        return {
            tableIsValid : tableValid,
            hasPlayedInTurn : playedInTurn,
            hasInitialPlay : initialPlay
        }
    }

    let invalidPlayerNameError = undefined;

    const testData = false;
    if (testData) setTestData();

    const unsubscribeLogging = log.subscribe(value => {
        writeLogMessage(value);
    });

    onDestroy(unsubscribeLogging);

    const messageHandlers = new Map();
    messageHandlers.set("Connected", (message) => {
        player = message.player;
        players.set(message.player.id, message.player);
        players = players;
        connection.sendJson(IDL.msgRequestPlayerList(player.id));
        connection.sendJson(IDL.msgRequestGameList(player.id));
        requestGamesInterval = setInterval(() => connection.sendJson(IDL.msgRequestGameList(player.id)), updateInterval);
        requestPlayersInterval = setInterval(() => connection.sendJson(IDL.msgRequestPlayerList(player.id)), updateInterval);
        writeLogMessage("Welcome to Rummikub!");

        invalidPlayerNameError = undefined;
    });
    messageHandlers.set("PlayerConnected", (message) => {
        players.set(message.player.id, message.player);
        players = players;
        writeLogMessage("Player joined: " + message.player.name);
    });
    messageHandlers.set("PlayerDisconnected", (message) => {
        players.delete(message.player.id);
        players = players;
        writeLogMessage("Player disconnected: " + message.player.name);
    });
    messageHandlers.set("PlayerNameExists", (message) => {
        invalidPlayerNameError = message.error;
    });
    messageHandlers.set("GameCreated", (message) => {
        games.set(message.game.gameName, message.game);
        games = games;
        if (message.game.owner === player.id)
            connection.sendJson(IDL.msgJoinGame(player.id, message.game.gameName));
        writeLogMessage("Game " + message.game.gameName + " created by " + players.get(message.game.owner).name);
    });
    messageHandlers.set("GameRemoved", (message) => {
        if (currentGame === message.gameName) {
            clearGame();
        }
        games.delete(message.gameName);
        games = games;
        writeLogMessage("Game " + message.gameName + " removed");
    });
    messageHandlers.set("PlayerJoinedGame", (message) => {
        if (message.playerId === player.id) {
            currentGame = message.gameName;
            connection.sendJson(IDL.msgRequestPlayerListForGame(player.id, currentGame));
        }
        playersInCurrentGame.push(players.get(message.playerId));
        playersInCurrentGame = playersInCurrentGame;
        writeLogMessage("Player " + players.get(message.playerId).name + " joined game " + message.gameName);
    });
    messageHandlers.set("PlayerLeftGame", (message) => {
        if (message.playerId === player.id)
            clearGame();
        playersInCurrentGame.splice(playersInCurrentGame.indexOf(message.playerId), 1);
        playersInCurrentGame = playersInCurrentGame
        writeLogMessage("Player " + players.get(message.playerId).name + " left game " + message.gameName);
    });
    messageHandlers.set("GameStarted", (message) => {
        updateGameState(message.gameName, message.gameState);

        if (currentGame !== undefined && currentGame === message.gameName)
            connection.sendJson(IDL.msgRequestGameState(player.id, currentGame));
        writeLogMessage("Game " + currentGame + " started");
    });
    messageHandlers.set("GameStopped", (message) => {
        updateGameState(message.gameName, message.gameState);

        writeLogMessage("Game " + message.gameName + " was stopped.");
    });
    messageHandlers.set("GameFinished", (message) => {
        writeLogMessage(players.get(message.winner).name + " wins the game!");
        updateGameState(message.gameName, message.gameState);
    });
    messageHandlers.set("GameSuspended", (message) => {
        writeLogMessage("Game " + games.get(message.gameName) + " suspended.");
        updateGameState(message.gameName, message.gameState);
    });
    messageHandlers.set("PlayedTilesHandToTable", (message) => {
        if (hand.has(message.tileSet.id)) {
            hand.delete(message.tileSet.id);
            table.set(message.tileSet.id, message.tileSet);
            hand = hand;
            table = table;
        }
    });
    messageHandlers.set("PlayedTilesTableToHand", (message) => {
        if (table.has(message.tileSet.id)) {
            table.delete(message.tileSet.id);
            hand.set(message.tileSet.id, message.tileSet);
            hand = hand;
            table = table;
        }
    });
    messageHandlers.set("TableChangedHandToTable", (message) => {
        table.set(message.tileSet.id, message.tileSet);
        table = table;
    });
    messageHandlers.set("TableChangedTableToHand", (message) => {
        if (table.has(message.tileSet.id)) {
            table.delete(message.tileSet.id);
            table = table;
        }
    });
    messageHandlers.set("PlayedTurnEnded", (message) => {
        currentPlayer = message.nextPlayerId;
        writeLogMessage("Turn ended. Next up is " + players.get(currentPlayer).name + ".");
    });
    messageHandlers.set("PlayedTookFromHeap", (message) => {
        hand.set(message.tileSet.id, message.tileSet);
        hand = hand;
    });
    messageHandlers.set("PlayerTookFromHeap", (message) => {
        writeLogMessage(players.get(message.playerId).name + " took a tile from the heap.");
    });
    messageHandlers.set("PlayedTileSetSplit", (message) => {
        if (message.location === LOCATION_HAND) {
            hand.delete(message.originalId);
            hand.set(message.leftSet.id, message.leftSet);
            hand.set(message.rightSet.id, message.rightSet);
            hand = hand;
        } else if (message.location === LOCATION_TABLE) {
            table.delete(message.originalId);
            table.set(message.leftSet.id, message.leftSet);
            table.set(message.rightSet.id, message.rightSet);
            table = table;
        }
    });
    messageHandlers.set("PlayedTileSetsMerged", (message) => {
        if (message.location === LOCATION_HAND) {
            hand.delete(message.sourceId);
            hand.delete(message.targetId);
            hand.set(message.tileSet.id, message.tileSet);
            hand = hand;
        } else if (message.location === LOCATION_TABLE) {
            table.delete(message.sourceId);
            table.delete(message.targetId);
            table.set(message.tileSet.id, message.tileSet);
            table = table;
        }
    });
    messageHandlers.set("PlayedTileSetsMovedAndMerged", (message) => {
        if (message.sourceLocation === LOCATION_TABLE) {
            table.delete(message.sourceId);
            hand.delete(message.targetId);
            hand.set(message.tileSet.id, message.tileSet);
        } else if (message.sourceLocation === LOCATION_HAND) {
            hand.delete(message.sourceId);
            table.delete(message.targetId);
            table.set(message.tileSet.id, message.tileSet);
        }
        table = table;
        hand = hand;
    });
    messageHandlers.set("TableChangedMovedAndMerged", (message) => {
        if (message.sourceLocation === LOCATION_TABLE) {
            table.delete(message.sourceId);
        } else if (message.sourceLocation === LOCATION_HAND) {
            table.delete(message.targetId);
            table.set(message.tileSet.id, message.tileSet);
        }
        table = table;
    });
    messageHandlers.set("MessageResponse", (message) => {
        writeLogMessage(message.message);
    });
    messageHandlers.set("GameListResponse", (message) => {
        games.clear();
        message.games.forEach(function (game) {
            games.set(game.gameName, game);
        });
        games = games;
    });
    messageHandlers.set("PlayerListResponse", (message) => {
        players.clear();
        message.players.forEach(function (player) {
            players.set(player.id, player);
        });
        players = players;
    });
    messageHandlers.set("PlayerListForGameResponse", (message) => {
        if (message.gameName === currentGame) {
            playersInCurrentGame = [];
            message.players.forEach(function (player) {
                playersInCurrentGame.push(player);
            });
            playersInCurrentGame = playersInCurrentGame;
        } else {
            playersInSelectedGame = [];
            message.players.forEach(function (player) {
                playersInSelectedGame.push(player);
            });
            playersInSelectedGame = playersInSelectedGame;
        }
    });
    messageHandlers.set("GameStateResponse", (message) => {
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
            playersInCurrentGame.push(player);
        });
        playersInCurrentGame = playersInCurrentGame;
        if (games.has(message.game.gameName)) {
            games.delete(message.game.gameName)
        }
        games.set(message.game.gameName, message.game);
        games = games;
    });
    messageHandlers.set("TurnState", (message) => {
        turnState = setTurnState(message.tableIsValid, message.hasPlayedInTurn, message.hasInitialPlay)
    });

    const receiveHandler = function (message) {
        if (!messageHandlers.has(message.messageType)) {
            console.log("Unknown message received from server: " + message.messageType);
            return;
        }
        messageHandlers.get(message.messageType)(message);
    }

    const closeHandler = function () {
        writeLogMessage("You were disconnected from the server.");
        clearState();
        // TODO cleanup client state
    }

    // window.addEventListener('beforeunload', (event) => {
    //     // Cancel the event as stated by the standard.
    //     event.preventDefault();
    //     // Chrome requires returnValue to be set.
    //     event.returnValue = 'If you navigate away, you close the session...';
    // });

    function clearState() {
        player = undefined;
        players.clear();
        players = players;
        games.clear();
        games = games;

        clearInterval(requestGamesInterval);
        clearInterval(requestPlayersInterval);

        clearGame();
    }

    function clearGame() {
        currentGame = undefined;
        currentPlayer = undefined;
        playersInCurrentGame = [];

        table.clear()
        table = table;
        hand.clear();
        hand = hand;
    }

    function updateGameState(gameName, newState) {
        if (games.has(gameName)) {
            games.get(gameName).gameState = gameState;
            games = games;
        }
    }

    function writeLogMessage(message) {
        logMessage.push(message);
        if (logMessage.length > maxNoOfLogMessages)
            logMessage.shift();
        logMessage = logMessage;
    }

    function eventJoin(event) {
        connection = new Connection(serverAddress, event.detail.playerName, receiveHandler, closeHandler);
    }

    function eventLeave(event) {
        connection.disconnect();
        clearState();
    }

    function eventJoinGame(event) {
        connection.sendJson(IDL.msgJoinGame(player.id, event.detail.gameName));
    }

    function eventLeaveGame(event) {
        connection.sendJson(IDL.msgLeaveGame(player.id, event.detail.gameName));
    }

    function eventCreateGame(event) {
        connection.sendJson(IDL.msgCreateGame(player.id, event.detail.gameName));
    }

    function eventRemoveGame(event) {
        connection.sendJson(IDL.msgRemoveGame(player.id, event.detail.gameName));
    }

    function eventStartGame(event) {
        connection.sendJson(IDL.msgStartGame(player.id, event.detail.gameName));
    }

    function eventStopGame(event) {
        connection.sendJson(IDL.msgStopGame(player.id, event.detail.gameName));
    }

    function eventMerge(event) {
        let msg;
        if (event.detail.sourceLocation === event.detail.targetLocation)
            msg = IDL.msgMerge(player.id, currentGame,
                event.detail.sourceId, event.detail.targetId, event.detail.index,
                event.detail.targetLocation);
        else
            msg = IDL.msgMoveAndMerge(player.id, currentGame,
                event.detail.sourceId, event.detail.targetId, event.detail.index,
                event.detail.sourceLocation, event.detail.targetLocation);

        connection.sendJson(msg);
    }

    function eventSplit(event) {
        const msg = IDL.msgSplit(player.id, currentGame, event.detail.id, event.detail.index, event.detail.location);
        connection.sendJson(msg);
    }

    function eventMoveTiles(event) {
        if (event.detail.targetLocation === LOCATION_TABLE) {
            connection.sendJson(IDL.msgHandToTable(player.id, currentGame, event.detail.id));
        } else if (event.detail.targetLocation === LOCATION_HAND) {
            connection.sendJson(IDL.msgTableToHand(player.id, currentGame, event.detail.id));
        }
    }

    function eventEndTurn(event) {
        connection.sendJson(IDL.msgEndTurn(player.id, currentGame));
    }

    function eventResetTurn(event) {
        connection.sendJson(IDL.msgResetTurn(player.id, currentGame));
    }

    function eventTakeFromHeap(event) {
        connection.sendJson(IDL.msgTakeFromHeap(player.id, currentGame));
    }

    function clickRequestGames(event) {
        connection.sendJson(IDL.msgRequestGameList(player.id));
    }

    function clickRequestPlayers(event) {
        connection.sendJson(IDL.msgRequestPlayerList(player.id));
    }

    function eventRequestPlayersInGameList(event) {
        connection.sendJson(IDL.msgRequestPlayerListForGame(player.id, event.detail.gameName));
    }

    function setTestData() {
        games.set("Aap",
            {
                gameName: "Aap",
                gameState:"STARTED"
            });
        games.set("Noot",
            {
                gameName: "Noot",
                gameState:"STARTED"
            });
        games.set("Mies",
            {
                gameName: "Mies",
                gameState:"STARTED"
            });
        players.set("p1", {id:"p1", name:"MyPlayer"});
        players.set("p2", {id:"p2", name:"OtherPlayer"});
        players.set("p3", {id:"p3", name:"AnotherPlayer"});

        player = players.get("p1");
        currentGame = "Aap";
        currentPlayer = "p1";
        playersInCurrentGame = [{id:"p1", name:"MyPlayer"}, {id:"p2", name:"OtherPlayer"}, {id:"p3", name:"AnotherPlayer"}];

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

        logMessage.push("Some message");
        logMessage.push("More text");
        logMessage.push("Very insightful, actually");
        logMessage.push("It just keeps on going...");
        logMessage = logMessage;
    }

    const welcomeHeader = [
        {char: 'R', color: 'black'},
        {char: 'U', color: 'yellow-600'},
        {char: 'M', color: 'blue-600'},
        {char: 'M', color: 'black'},
        {char: 'I', color: 'red-600'},
        {char: 'K', color: 'yellow-600'},
        {char: 'U', color: 'red-600'},
        {char: 'B', color: 'blue-600'}];
</script>

<div id="fullscreen-container" class="full-screen-app relative app-layout w-screen bg-gray-900">
    <header id="header" class="header-area h-16 bg-blue-600 p-1 flex flex-no-wrap items-stretch justify-between">
        <div class="flex flex-row flex-start">
            {#each welcomeHeader as tile}
                <div class="w-12 border border-gray-300 rounded bg-orange-200 text-center align-text-top text-3xl text-{tile.color}">
                    {tile.char}
                </div>
            {/each}
        </div>
        <div class="text-left font-inter text-white h-full grid grid-cols-1 grid-rows-2">
            <div class="text-left align-middle font-inter text-white">
            {#if player}
                <span class="text-gray-500">Player: </span>{player.name}
            {/if}
            </div>
            <div class="text-left font-inter text-white">
            {#if currentGame}
                <span class="text-gray-500">Game: </span>{currentGame}
            {/if}
            </div>
        </div>
        <div class="h-full w-1/4 grid grid-cols-2 grid-rows-2">
        {#each playersInCurrentGame as playerInGame}
            <div class="px-2 text-gray-500 text-left align-middle" class:text-orange-500={playerInGame.id===currentPlayer}>{playerInGame.name}</div>
        {/each}
        </div>
        <RegisterPlayer {player} {invalidPlayerNameError} on:connect={eventJoin} on:disconnect={eventLeave}/>
    </header>
    <div class="board-area relative my-1 mx-1">
        <TileArea tiles="{table}" areaLocation={LOCATION_TABLE} isPlayersTurn="{isPlayersTurn}"
                  on:merge={eventMerge}
                  on:split={eventSplit}
                  on:moveTiles={eventMoveTiles}/>
        <SlidingSidebar auto_minimize="true">
            <header slot="label">Games</header>
            <div class="sidebar-content-height" slot="content">
                <GameList games="{games}" currentGame="{currentGame}" player="{player}" playersInSelectedGame="{playersInSelectedGame}"
                           on:gameDetails={eventRequestPlayersInGameList}
                           on:joinGame={eventJoinGame} on:leaveGame={eventLeaveGame}
                           on:createGame={eventCreateGame}
                           on:removeGame={eventRemoveGame}
                           on:startGame={eventStartGame}
                           on:stopGame={eventStopGame}
                />
            </div>
        </SlidingSidebar>
    </div>
    <div class="hand-area relative h-48 mb-1 mx-1">
        <TileArea tiles="{hand}" areaLocation={LOCATION_HAND} isPlayersTurn="{true}"
          on:merge={eventMerge}
          on:split={eventSplit}
          on:moveTiles={eventMoveTiles}/>
        <SlidingSidebar auto_minimize="true">
            <header slot="label">Turn</header>
            <div class="sidebar-content-height" slot="content">
                <TurnControls {gameState} {isPlayersTurn}
                              on:endTurn={eventEndTurn}
                              on:resetTurn={eventResetTurn}
                              on:takeFromHeap={eventTakeFromHeap}
                />
            </div>
        </SlidingSidebar>
    </div>
    <footer id="footer" class="footer-area h-10 bg-blue-600 p-2 flex flex-col-reverse overflow-y-auto">
        {#each logMessage.slice().reverse() as message}
            <div class="font-inter text-white">{message}</div>
        {/each}
    </footer>
</div>

<style>
    .full-screen-app {
        @apply h-screen w-screen overflow-x-hidden;
        min-height: -webkit-fill-available;
    }
    .app-layout {
        @apply grid;
        grid-template-areas:    'header'
                                'board'
                                'hand'
                                'footer';
        grid-template-columns: 5fr;
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
    .sidebar-content-height {
        /* Needed because the sidebar is translated by 2 rem */
        height: calc(100% - 2rem);
    }
</style>

<!--UX:-->
<!-- - mechanics when someone wins a game (leave / stop game)-->
<!-- - styling of buttons-->
<!-- - review ability to click game and turn control buttons in all game states (using TurnState object)-->
<!-- - show size of the heap-->
<!-- - prevent page from navigating away-->

<!--Logic:-->
<!-- - GameLogic: Implement scoring mechanism when a player wins-->
<!-- - GameLogic: Implement finish condition when no more players can make a valid move and heap is empty.-->
<!-- - GameLogic: cannot drag back a tileset containing a joker from the table to hand (when placed there yourself in same turn); because a joker is not counted as part of the played tiles list...-->