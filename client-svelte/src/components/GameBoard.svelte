<script>
    import TileSet from "./TileSet.svelte";
    import { createEventDispatcher } from 'svelte';
    import { LOCATION_TABLE, TILESET_SOURCE_LOCATION_TYPE, createMoveTilesEvent } from "../utils/GameUtils.js";

    const dispatch = createEventDispatcher();

    export let table;
    export let isPlayersTurn;

    function eventMoveTilesToTable(event) {
        if (event.dataTransfer.getData(TILESET_SOURCE_LOCATION_TYPE)!==LOCATION_TABLE) {
            const payload = createMoveTilesEvent(event, LOCATION_TABLE);
            dispatch(payload.eventName, payload);
        }
    }

    function eventDragOver(event) {
        event.preventDefault();
    }
</script>

<div id="gameBoard"
        on:drop={eventMoveTilesToTable}
        on:dragover={eventDragOver}
        class="flex flex-row flex-wrap items-start content-start h-full"
        class:active-player={isPlayersTurn}
        class:inactive-player={!isPlayersTurn}>
    {#if table.size === 0 }
        <span class="text-xl text-gray-400">No tiles are on the table.</span>
    {/if}
    {#each [...table] as [id, tileSet]}
        <TileSet {tileSet} location="TABLE" on:merge on:split></TileSet>
    {/each}
</div>

<style>
    .active-player {
        @apply border-2 border-orange-500 rounded-md;
    }
    .inactive-player {
        padding: 2px;
    }
</style>