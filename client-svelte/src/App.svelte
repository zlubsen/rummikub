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

    let playerId = undefined;
    let playerName = undefined;
    let currentGame = undefined;
    $: gameState = games.has(currentGame) ? games.get(currentGame).gameState : null;
    $: isPlayersTurn = currentPlayer !== undefined && currentPlayer === playerId;

    let connection;
    let logMessage;

    let players = new Map();
    let games = new Map();

    let currentPlayer = undefined;
    let table = new Map();
    let hand = new Map();

    const receiveHandler = function (message) {
        let messageType = message.messageType
        console.log("received msg: " + message.messageType);
        switch (messageType) {
            case "MessageResponse":
                logMessage = message.message;
                break;
            case "Connected":
                playerId = message.player.id
                playerName = message.player.name
                connection.sendJson(IDL.msgRequestPlayerList(playerId));
                connection.sendJson(IDL.msgRequestGameList(playerId));
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
                logMessage = "Player left: " + message.player.name;
                break;
            case "GameCreated":
                games.set(message.game.gameName, message.game);
                games = games;
                if (message.game.owner === playerId)
                    connection.sendJson(IDL.msgJoinGame(playerId, message.game.gameName));
                logMessage = "Game " + message.game.gameName + " created by " + players.get(message.game.owner);
                break;
            case "GameRemoved":
                games.delete(message.gameName);
                games = games;
                if (currentGame === message.gameName) {
                    currentGame = undefined;
                }
                logMessage = "Game " + message.gameName + " removed";
                break;
            case "PlayerJoinedGame":
                if (message.playerId === playerId)
                    currentGame = message.gameName;
                logMessage = "Player " + players.get(message.playerId) + " joined game " + message.gameName;
                break;
            case "PlayerLeftGame":
                if (message.playerId === playerId)
                    currentGame = undefined;
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
                }
                break;
            case "GameFinished":
                // TODO
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
                    hand.delete(message.leftId);
                    hand.delete(message.rightId);
                    hand.set(message.tileSet.id, message.tileSet);
                    hand = hand;
                } else if (message.location === "TABLE") {
                    table.delete(message.leftId);
                    table.delete(message.rightId);
                    table.set(message.tileSet.id, message.tileSet);
                    table = table;
                }
                break;
            case "GameListResponse":
                games.clear();
                message.games.forEach(function (gameName) {
                    games.set(gameName, gameName);
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
        console.log("disconnected from server");
        // TODO cleanup client state
    }

    let game = {
    	gameName : "game1",
    	currentPlayer : "player1",
    	board : [
    		{
    			id: "xxsdf1",
    			tiles : [
    				{color : "gray", number : "8", isJoker: false},
    				{color : "gray", number : "9", isJoker: false},
    				{color : "gray", number : "10", isJoker: false}
    			]
    		},
    		{
    			id: "ldjfb5",
    			tiles : [
    				{color : "blue", number : "1", isJoker: false},
    				{color : "yellow", number : "1", isJoker: false},
    				{color : "gray", number : "1", isJoker: false}
    			]
    		},
    		{
    			id: "sdlkg4",
    			tiles : [
    				{color : "blue", number : "8", isJoker: false},
    				{color : "gray", number : "3", isJoker: false},
    				{color : "red", number : "10", isJoker: false},
    				{color : "red", number : "10", isJoker: true}
    			]
    		}
    	]
    };

    hand.set("xxsdf1",
    	{
    		id: "xxsdf1",
    		tiles : [
    			{color : "BLACK", number : "8", isJoker: false},
    			{color : "BLACK", number : "9", isJoker: false},
    			{color : "BLACK", number : "10", isJoker: false}
    		],
            isValid:true
    	});
    hand.set("ldjfb5",
    	{
    		id: "ldjfb5",
    		tiles : [
    			{color : "BLUE", number : "1", isJoker: false},
    			{color : "YELLOW", number : "1", isJoker: false},
    			{color : "BLACK", number : "1", isJoker: false}
    		],
            isValid:true
    	});
    hand.set("sdlkg4",
    	{
    		id: "sdlkg4",
    		tiles : [
    			{color : "BLUE", number : "8", isJoker: false},
    			{color : "BLACK", number : "3", isJoker: false},
    			{color : "RED", number : "10", isJoker: false},
    			{color : "RED", number : "10", isJoker: true}
    		],
            isValid:false
    	});
    hand.set("livnr5",
            {
                id: "livnr5",
                tiles : [
                    {color : "BLUE", number : "6", isJoker: false},
                    {color : "BLUE", number : "7", isJoker: false},
                    {color : "BLUE", number : "8", isJoker: false},
                    {color : "BLUE", number : "9", isJoker: false},
                    {color : "BLUE", number : "10", isJoker: false},
                    {color : "BLUE", number : "11", isJoker: false},
                    {color : "BLUE", number : "12", isJoker: false},
                    {color : "BLUE", number : "13", isJoker: false},
                ],
                isValid:true
            });

    function eventJoin(event) {
        playerName = event.detail.playerName;
        connection = new Connection(undefined, playerName, receiveHandler, closeHandler);
    }

    function eventLeave(event) {
        connection.disconnect();
        playerName = undefined;
        playerId = undefined;
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
        const msg = IDL.msgMerge(playerId, currentGame, event.detail.leftId, event.detail.rightId, event.detail.location);
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

    function eventTakeFromHeap(event) {
        connection.sendJson(IDL.msgTakeFromHeap(playerId, currentGame));
    }

    function clickRequestGames(event) {
        connection.sendJson(IDL.msgRequestGameList(playerId));
    }

    function clickRequestPlayers(event) {
        connection.sendJson(IDL.msgRequestPlayerList(playerId));
    }
</script>

<main class="h-full">
    <header id="header" class="min-w-full h-16 border border-blue-300 rounded m-1 p-3">
        <span class="bold">Welcome to Rummikub!</span>
        {#if playerName}
            <span class="pl-2 pr-2">Player: {playerName}</span>
        {/if}
        {#if currentGame}
            <span class="pl-2 pr-2">Game: {currentGame}</span>
        {/if}
        <RegisterPlayer playerId="{playerId}" on:connect={eventJoin} on:disconnect={eventLeave}/>
    </header>
    <section id="game" class="min-h-64 flex flex-wrap m-1">
            <GameBoard table="{table}"
                on:merge={eventMerge}
                on:split={eventSplit}
                on:moveTiles="{eventMoveTiles}"
            />
            <GameList games="{games}" currentGame="{currentGame}" playerId="{playerId}"
                on:joinGame={eventJoinGame} on:leaveGame={eventLeaveGame}
                on:createGame={eventCreateGame}
                on:removeGame={eventRemoveGame}
                on:startGame={eventStartGame}
                on:stopGame={eventStopGame}
            />
            <PlayerHand hand="{hand}"
                on:merge={eventMerge}
                on:split={eventSplit}
                on:moveTiles={eventMoveTiles}
            />
            <TurnControls {gameState} {isPlayersTurn}
                on:endTurn={eventEndTurn}
                on:takeFromHeap={eventTakeFromHeap}
            />
    </section>
    <footer id="footer" class="min-w-full h-8 border border-blue-300 rounded m-1 p-3">
        {#if logMessage}
            {logMessage}
        {/if}
    </footer>

    <div id="admin_section" class="my-2 mx-2 p-2">
        <button on:click={clickRequestGames}>Request Games</button>
        <button on:click={clickRequestPlayers}>Request Players</button>
    </div>
</main>

<style>
</style>

<!-- TODO show/hide buttons and such based on gamestate -->
<!-- V UX: move tiles from same location: do not send message to server; make it impossible-->
<!-- - can we merge with not owned tilesets during initial play?-->
<!-- - UX: directly move tileset location and merge with other tileset (e.g., directly append a tile to a set on the table)-->
<!-- - tried to merge a set with a J, lost the set on the table (‘TileSet not found in TABLE.’), and also in the hand…-->
<!-- - UX: merge fields stay visible when not dragging a tileset (dragEnd is not called)-->
<!-- - Logic for checking victory conditions (check when moving / manipulating tiles, on end turn?)-->