<script>
    import { createEventDispatcher } from 'svelte';
    import SelectableList from "../components/SelectableList.svelte";

    const dispatch = createEventDispatcher();

    export let games;
    export let currentGame;
    export let player;
    export let playersInSelectedGame;
    let selectedGame = null;
    let createGameName;
    $: isOwner = currentGame !== undefined && games.has(currentGame) && games.get(currentGame).owner == player.id;
    $: currentIsStarted = currentGame !== undefined && games.has(currentGame) && games.get(currentGame).gameState === "STARTED";

    function clickGameList(event) {
        dispatch('gameDetails', {
            gameName : selectedGame
        })
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
                gameName : currentGame
            });
        }
    }
    function clickCreateGame(event) {
        dispatch('createGame', {
            gameName : createGameName
        });
        createGameName = undefined;
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

<div id="gameList" class="bg-blue-600 h-full p-2">
    <SelectableList on:select={clickGameList} items="{[...games.values()]}" bind:selectedItem={selectedGame} />
    {#if (selectedGame && (playersInSelectedGame !== undefined)) }
        <ul class="border h-24">
        {#each playersInSelectedGame as player}
            <li>{player.name}</li>
        {/each}
        </ul>
    {/if}
    {#if currentGame }
        {#if isOwner }
            {#if currentIsStarted }
                <button id="stopGameButton" on:click={clickStopGame} class="form-input m-1">Stop game</button>
                <br/>
            {:else}
                <button id="startGameButton" on:click={clickStartGame} class="form-input m-1">Start game</button>
                <br/>
                <button id="removeGameButton" on:click={clickRemoveGame} class="form-input m-1">Remove game '{currentGame}'</button>
                <br/>
            {/if}
        {:else}
            <button id="leaveGameButton" on:click={clickLeaveGame} class="form-input m-1">Leave Game '{currentGame}'</button>
            <br/>
        {/if}
    {:else}
        <button id="joinGameButton" on:click={clickJoinGame} disabled="{!selectedGame || currentGame}" class="form-input m-1">Join Game</button>
        <br/>

        <input type="text" id="createGame" placeholder="Create new game..." bind:value={createGameName} disabled="{!player}" class="form-input w-full m-1">
        <br/>
        <button id="createGameButton" on:click={clickCreateGame} disabled="{!player||!createGameName}" class="form-input m-1">Create game</button>
    {/if}
</div>

<style>

</style>