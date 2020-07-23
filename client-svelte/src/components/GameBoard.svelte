<script>
    import TileSet from "./TileSet.svelte";
    import { createEventDispatcher } from 'svelte';
    import { LOCATION_TABLE, TILESET_SOURCE_LOCATION_TYPE, createMoveTilesEvent } from "../utils/GameUtils.js";

    const dispatch = createEventDispatcher();

    export let table;

    function eventMoveTilesToTable(event) {
        console.log("eventMoveTilesToTable");
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
        class="w-3/4 h-3/5 border border-blue-300">
    The board:
    {#each [...table] as [id, tileSet]}
        <TileSet {tileSet} location="TABLE" on:merge on:split></TileSet>
    {/each}
</div>

<style></style>