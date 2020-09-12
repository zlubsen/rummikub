<script>
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    export let gameState;
    export let isPlayersTurn;

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
            <button id="endTurnButton" disabled="{!isPlayersTurn}" on:click={clickEndTurn} class="form-input">End Turn</button>
            <button id="resetButton" disabled="{!isPlayersTurn}" on:click={clickResetTurn} class="form-input">Reset</button>
            <button id="takeFromHeapButton" disabled="{!isPlayersTurn}" on:click={clickTakeFromHeap} class="form-input">Take from Heap</button>
        </div>
    {:else}
        No action available
    {/if}
</div>

<style>
</style>