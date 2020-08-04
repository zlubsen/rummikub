<script>
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    export let games;
    export let currentGame;
    export let playerId;
    let selectedGame;
    let createGameName;
    $: isOwner = currentGame !== undefined && games.has(currentGame) && games.get(currentGame).owner == playerId;
    $: currentIsStarted = currentGame !== undefined && games.has(currentGame) && games.get(currentGame).gameState === "STARTED";

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
                gameName : currentGame
            });
        }
    }
    function clickCreateGame(event) {
        dispatch('createGame', {
            gameName : createGameName
        });
    }

    function clickRemoveGame(event) {
        dispatch('removeGame', {
            gameName : currentGame
        });
    }

    function clickStartGame(event) {
        dispatch('startGame', {
            gameName : currentGame
        });
    }

    function clickStopGame(event) {
        dispatch('stopGame', {
            gameName : currentGame
        });
    }
</script>

<div id="gameList" class="w-1/4">
    <ul>
        <select id="games" size="10" bind:value={selectedGame} class="form-multiselect w-20">
        {#each [...games] as [id, game]}
            <option value="{game.gameName}">{game.gameName}</option>
        {/each}
        </select>
        {#if currentGame }
            {#if isOwner }
                {#if currentIsStarted }
                    <button id="stopGameButton" on:click={clickStopGame}>Stop game</button><br/>
                {:else}
                    <button id="startGameButton" on:click={clickStartGame}>Start game</button><br/>
                    <button id="removeGameButton" on:click={clickRemoveGame}>Remove game '{currentGame}'</button><br/>
                {/if}
            {:else}
                <button id="leaveGameButton" on:click={clickLeaveGame}>Leave Game '{currentGame}'</button><br/>
            {/if}
        {:else}
            <button id="joinGameButton" on:click={clickJoinGame} disabled="{currentGame}">Join Game</button><br/>

            <input type="text" id="createGame" placeholder="Create new game..." bind:value={createGameName} class="form-input">
            <button id="createGameButton" on:click={clickCreateGame}>Create game</button>
        {/if}



    </ul>
</div>

<style>

</style>