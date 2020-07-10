<script>
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    export let games;
    export let currentGame;
    let selectedGame;

    function onSelectGame() {
        const select = document.querySelector("games");
        const selectedValue = select.value;
        if (selectedValue !== "")
            selectedGame = id;
    }

    function clickJoinGame(event) {
        if (selectedGame) {
            dispatch('joinGame', {
                gameName : selectedGame
            });
        }
    }
    function clickLeaveGame(event) {
        if (selectedGame) {
            dispatch('leaveGame', {
                gameName : selectedGame
            });
        }
    }
</script>

<ul>
<!--    <span style="@apply bg-black text-white">TODO: list of games here</span>-->
    <select id="games" size="10">
    {#each [...games] as {id, playerName}}
        <option value="{id}" on:click={onSelectGame} style="@apply bg-black text-white">{gameName}</option>
    {/each}
    </select>
    <button id="joinGameButton" on:click={clickJoinGame}>Join Game</button>
    {#if currentGame !== false}
        <button id="leaveGameButton" on:click={clickLeaveGame}>Leave Game</button>
</ul>

<style>

</style>