<script>
    import TileSet from "./TileSet.svelte";
    import { createEventDispatcher } from 'svelte';
    import { LOCATION_TABLE, TILESET_SOURCE_LOCATION_TYPE, createMoveTilesEvent } from "../utils/GameUtils.js";

    const dispatch = createEventDispatcher();

    export let table;

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
        class="flex flex-row flex-wrap flex-auto w-3/4 h-full my-1 p-2 border-2 border-blue-300 rounded-md">
    {#if table.size === 0 }
        No tiles are on the table.
    {/if}
    {#each [...table] as [id, tileSet]}
        <TileSet {tileSet} location="TABLE" on:merge on:split></TileSet>
    {/each}
</div>

<style></style>