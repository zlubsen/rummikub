<script>
    import Tailwindcss from './Tailwindcss.svelte';
    import { Connection } from "./utils/Connection.js";
    import * as IDL from "./idl/ClientMessages.js";
    import RegisterPlayer from "./components/RegisterPlayer.svelte";
    import GameBoard from "./components/GameBoard.svelte";
    import GameList from "./components/GameList.svelte";
    import PlayerHand from "./components/PlayerHand.svelte";

    let playerId;
    let playerName;
    let currentGame;

    let connection;
    let logMessage;

    let players = new Map();
    let games = new Map();

    let game = {};
    let hand = [];

    const receiveHandler = function (message) {
        let messageType = message.messageType
        switch (messageType) {
            case "MessageResponse":
                logMessage = message.message;
                break;
            case "Connected":
                playerId = message.player.id
                playerName = message.player.name
                connection.sendJson(IDL.msgRequestPlayerList(playerId));
                connection.sendJson(IDL.msgRequestGameList(playerId));
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
                games.set(message.game.name, message.game);
                games = games;
                break;
            case "GameRemoved":
                games.delete(message.game.gameName);
                games = games;
                break;
            case "PlayerJoinedGame":
                break;
            case "PlayerLeftGame":
                break;
            case "GameStarted":
                break;
            case "GameStopped":
                break;
            case "GameFinished":
                break;
            case "PlayedTilesHandToTable":
                break;
            case "PlayedTilesTableToHand":
                break;
            case "PlayedTurnEnded":
                break;
            case "PlayedTookFromHeap":
                break;
            case "PlayedTileSetSplit":
                break;
            case "PlayedTileSetsMerged":
                break;
            case "GameListResponse":
                games.clear();
                message.games.forEach( function (gameName) { games.set(gameName, gameName); });
                games = games;
                break;
            case "PlayerListResponse":
                players.clear();
                message.players.forEach( function (player) { players.set(player.id, player.name); });
                players = players;
                break;
            default:
                console.log("Unknown message received from server: " + message);
                break;
        }
    }

    // let game = {
    // 	gameName : "game1",
    // 	currentPlayer : "player1",
    // 	board : [
    // 		{
    // 			id: "xxsdf1",
    // 			tiles : [
    // 				{color : "gray", number : "8", isJoker: false},
    // 				{color : "gray", number : "9", isJoker: false},
    // 				{color : "gray", number : "10", isJoker: false}
    // 			]
    // 		},
    // 		{
    // 			id: "ldjfb5",
    // 			tiles : [
    // 				{color : "blue", number : "1", isJoker: false},
    // 				{color : "yellow", number : "1", isJoker: false},
    // 				{color : "gray", number : "1", isJoker: false}
    // 			]
    // 		},
    // 		{
    // 			id: "sdlkg4",
    // 			tiles : [
    // 				{color : "blue", number : "8", isJoker: false},
    // 				{color : "gray", number : "3", isJoker: false},
    // 				{color : "red", number : "10", isJoker: false},
    // 				{color : "red", number : "10", isJoker: true}
    // 			]
    // 		}
    // 	]
    // };
    // const hand = [
    // 	{
    // 		id: "xxsdf1",
    // 		tiles : [
    // 			{color : "gray", number : "8", isJoker: false},
    // 			{color : "gray", number : "9", isJoker: false},
    // 			{color : "gray", number : "10", isJoker: false}
    // 		]
    // 	},
    // 	{
    // 		id: "ldjfb5",
    // 		tiles : [
    // 			{color : "blue", number : "1", isJoker: false},
    // 			{color : "yellow", number : "1", isJoker: false},
    // 			{color : "gray", number : "1", isJoker: false}
    // 		]
    // 	},
    // 	{
    // 		id: "sdlkg4",
    // 		tiles : [
    // 			{color : "blue", number : "8", isJoker: false},
    // 			{color : "gray", number : "3", isJoker: false},
    // 			{color : "red", number : "10", isJoker: false},
    // 			{color : "red", number : "10", isJoker: true}
    // 		]
    // 	}
    // ];
    function requestGamesMessage() {
        return JSON.stringify({
            messageType : "RequestPlayerList",
            playerId : playerId
        });
    }

    function requestPlayersMessage() {
        return JSON.stringify({
            messageType : "RequestGameList",
            playerId : playerId
        });
    }

    function eventJoin(event) {
        playerName = event.detail.playerName;
        connection = new Connection(undefined, playerName, receiveHandler);
    }

    function eventLeave(event) {
        connection.disconnect();
        playerName = undefined;
        playerId = undefined;
    }

    function eventJoinGame(event) {
        console.log("join game: " + event.detail.gameName);
        connection.sendJson(IDL.msgJoinGame(playerId, event.detail.gameName))
    }

    function eventLeaveGame(event) {
        console.log("leave game: " + event.detail.gameName);
        connection.sendJson(IDL.msgLeaveGame(playerId, event.detail.gameName))
    }

    function clickRequestGames(event) {
        const msg = IDL.msgRequestGameList(playerId);
        connection.sendJson(msg);
    }

    function clickRequestPlayers(event) {
        const msg = IDL.msgRequestPlayerList(playerId);
        connection.sendJson(msg);
    }
</script>

<main>
    <button on:click={clickRequestGames}>Request Games</button>
    <button on:click={clickRequestPlayers}>Request Players</button>

    <RegisterPlayer playerId="{playerId}" on:connect={eventJoin} on:disconnect={eventLeave}/>
    <!--	<GameBoard></GameBoard>-->
    <GameList games="{games}" currentGame="{currentGame}" on:joinGame={eventJoinGame} on:leaveGame={eventLeaveGame}></GameList>
    <PlayerHand hand="{hand}"></PlayerHand>

    <div>{logMessage}</div>
</main>