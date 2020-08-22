<script>
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    export let gameState;
    export let isPlayersTurn;

    function clickEndTurn(event) {
        dispatch('endTurn', {
        });
    }

    function clickTakeFromHeap(event) {
        dispatch('takeFromHeap', {
        });
    }
</script>

<div id="turnControls" class="h-full p-1 bg-blue-600">
Controls<br/>
    {#if gameState === "STARTED" }
        <button id="endTurnButton" disabled="{!isPlayersTurn}" on:click={clickEndTurn} class="form-input">End Turn</button><br/>
        <button id="takeFromHeapButton" disabled="{!isPlayersTurn}" on:click={clickTakeFromHeap} class="form-input">Take from Heap</button><br/>
    {/if}
</div>

<style>
</style>