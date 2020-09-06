<script>
    import { createEventDispatcher } from 'svelte';
    import { flip } from 'svelte/animate';
    import { quintOut } from 'svelte/easing';
    import TileSet from "./TileSet.svelte";
    import { TILESET_SOURCE_LOCATION_TYPE, createMoveTilesEvent } from "../utils/GameUtils.js";

    const dispatch = createEventDispatcher();

    export let tiles;
    export let areaLocation;
    export let isPlayersTurn;

    function eventMoveTiles(event) {
        if (event.dataTransfer.getData(TILESET_SOURCE_LOCATION_TYPE)!==areaLocation) {
            const payload = createMoveTilesEvent(event, areaLocation);
            dispatch(payload.eventName, payload);
        }
    }

    function eventDragOver(event) {
        event.preventDefault();
    }

</script>

<div id="tileArea-{areaLocation}"
     on:drop={eventMoveTiles}
     on:dragover={eventDragOver}
     class="flex flex-row flex-wrap items-start content-start h-full overflow-y-auto"
     class:active-player={isPlayersTurn}
     class:inactive-player={!isPlayersTurn}>
    {#if tiles.size === 0 }
        <span class="text-xl text-gray-400 p-2">No tiles in this area.</span>
    {/if}
    {#each [...tiles] as [id, tileSet] (id)}
        <div animate:flip="{{delay: 250, duration: 250, easing: quintOut}}">
            <TileSet {tileSet} location="{areaLocation}" on:merge on:split></TileSet>
        </div>
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