<script>
	import { connection } from "./connection.js";
	import Tailwindcss from './Tailwindcss.svelte';
	import RegisterPlayer from "./components/RegisterPlayer.svelte";
	import GameBoard from "./components/GameBoard.svelte";
	import GameList from "./components/GameList.svelte";
	import PlayerHand from "./components/PlayerHand.svelte";

	let socket;
	let playerId;
	let playerName;

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
	const hand = [
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
	];
</script>

<main>
	<RegisterPlayer />
<!--	<GameBoard></GameBoard>-->
<!--	<GameList></GameList>-->
	<PlayerHand hand="{hand}"></PlayerHand>
</main>