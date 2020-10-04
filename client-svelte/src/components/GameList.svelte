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

<div id="gameList" class="bg-blue-600 h-full p-1 pr-2 flex flex-col justify-start items-stretch">
    <div class="relative">
    <SelectableList on:select={clickGameList} items="{[...games.values()]}" bind:selectedItem={selectedGame} />
    {#if (selectedGame && (playersInSelectedGame !== undefined || playersInSelectedGame.size > 0 )) }
        <div class="absolute inset-x-0 bottom-0 bg-blue-400">
            <ul class="h-24 font-inter text-base text-white">
            {#each playersInSelectedGame as player}
                <li>{player.name}</li>
            {/each}
            </ul>
        </div>
    {/if}
    </div>
    <div class="my-1 flex flex-col justify-between">
    {#if currentGame }
        {#if isOwner }
            {#if currentIsStarted }
                <button id="stopGameButton" on:click={clickStopGame} class="px-4 py-2 border border-transparent text-sm leading-5 font-medium text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 active:bg-indigo-700 transition duration-150 ease-in-out">Stop game</button>
            {:else}
                <button id="startGameButton" on:click={clickStartGame} class="px-4 py-2 border border-transparent text-sm leading-5 font-medium text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 active:bg-indigo-700 transition duration-150 ease-in-out">Start game '{currentGame}'</button>
                <button id="removeGameButton" on:click={clickRemoveGame} class="px-4 py-2 border border-transparent text-sm leading-5 font-medium text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 active:bg-indigo-700 transition duration-150 ease-in-out">Remove game '{currentGame}'</button>
            {/if}
        {:else}
            <button id="leaveGameButton" on:click={clickLeaveGame} class="px-4 py-2 border border-transparent text-sm leading-5 font-medium text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 active:bg-indigo-700 transition duration-150 ease-in-out">Leave Game '{currentGame}'</button>
        {/if}
    {:else}
        <button id="joinGameButton" on:click={clickJoinGame} disabled="{!selectedGame || currentGame}" class="px-4 py-2 border border-transparent text-sm leading-5 font-medium text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 active:bg-indigo-700 transition duration-150 ease-in-out">Join Game</button>
        <input type="text" id="createGame" placeholder="Create new game..." bind:value={createGameName} disabled="{!player}" class="mt-2 px-4 py-2 border border-transparent text-sm leading-5 font-medium text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 active:bg-indigo-700 transition duration-150 ease-in-out">
        <button id="createGameButton" on:click={clickCreateGame} disabled="{!player||!createGameName}" class="px-4 py-2 border border-transparent text-sm leading-5 font-medium text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 active:bg-indigo-700 transition duration-150 ease-in-out">Create game</button>
    {/if}
    </div>
</div>