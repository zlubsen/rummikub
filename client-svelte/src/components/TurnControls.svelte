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
            <button id="endTurnButton" disabled="{!isPlayersTurn || !turnState.tableIsValid || !turnState.hasPlayedInTurn}" on:click={clickEndTurn} class="">End Turn</button>
            <button id="resetButton" disabled="{!isPlayersTurn || !turnState.hasPlayedInTurn}" on:click={clickResetTurn} class="">Reset</button>
            <button id="takeFromHeapButton" disabled="{!isPlayersTurn}" on:click={clickTakeFromHeap} class="">Take from Heap</button>
        </div>
    {:else}
        No action available
    {/if}
</div>

<style>
</style>