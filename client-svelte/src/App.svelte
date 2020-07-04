<script>
	import RegisterPlayer from "./components/RegisterPlayer.svelte";
	import GameBoard from "./components/GameBoard.svelte";
	import GameList from "./components/GameList.svelte";
	import PlayerHand from "./components/PlayerHand.svelte";

	let socket;
	let playerId;
	let playerName;
</script>

<main>
	<RegisterPlayer />
<!--	<GameBoard></GameBoard>-->
<!--	<GameList></GameList>-->
	<PlayerHand></PlayerHand>
</main>

<style>
	/*main {*/
	/*	text-align: center;*/
	/*	padding: 1em;*/
	/*	max-width: 240px;*/
	/*	margin: 0 auto;*/
	/*}*/

	/*h1 {*/
	/*	color: #ff3e00;*/
	/*	text-transform: uppercase;*/
	/*	font-size: 4em;*/
	/*	font-weight: 100;*/
	/*}*/

	/*@media (min-width: 640px) {*/
	/*	main {*/
	/*		max-width: none;*/
	/*	}*/
	/*}*/
</style>