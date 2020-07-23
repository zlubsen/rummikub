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

{#if gameState === "STARTED" }
<div id="turnControls" class="w-1/4">
    <button id="endTurnButton" disabled="{!isPlayersTurn}" on:click={clickEndTurn}>End Turn</button><br/>
    <button id="takeFromHeapButton" disabled="{!isPlayersTurn}" on:click={clickTakeFromHeap}>Take from Heap</button><br/>
</div>
{/if}

<style>
</style>