<script>
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    export let gameState;
    export let isPlayersTurn;
    export let turnState;

    function clickEndTurn(event) {
        dispatch('endTurn', {});
    }

    function clickResetTurn(event) {
        dispatch('resetTurn',{});
    }

    function clickTakeFromHeap(event) {
        dispatch('takeFromHeap', {});
    }
</script>

<div id="turnControls" class="h-full p-1 pr-2 bg-blue-600">
    {#if gameState === "STARTED" }
        <div class="h-full flex flex-col justify-around items-stretch">
            <button id="endTurnButton" disabled="{!isPlayersTurn || !turnState.tableIsValid || !turnState.hasPlayedInTurn}" on:click={clickEndTurn} class="px-4 py-2 border border-transparent text-sm leading-5 font-medium text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 active:bg-indigo-700 transition duration-150 ease-in-out">End Turn</button>
            <button id="resetButton" disabled="{!isPlayersTurn || !turnState.hasPlayedInTurn}" on:click={clickResetTurn} class="px-4 py-2 border border-transparent text-sm leading-5 font-medium text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 active:bg-indigo-700 transition duration-150 ease-in-out">Reset</button>
            <button id="takeFromHeapButton" disabled="{!isPlayersTurn}" on:click={clickTakeFromHeap} class="px-4 py-2 border border-transparent text-sm leading-5 font-medium text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:shadow-outline-indigo focus:border-indigo-700 active:bg-indigo-700 transition duration-150 ease-in-out">Take from Heap ({turnState.noOfTilesInHeap})</button>
        </div>
    {:else}
        No action available
    {/if}
</div>