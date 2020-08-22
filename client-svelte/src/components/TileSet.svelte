<script>
    import { createEventDispatcher, onDestroy } from 'svelte';
    import { scale } from 'svelte/transition';
    import { quintOut } from 'svelte/easing';
    import Tile from "./Tile.svelte";
    import { dragging } from "../stores/dragTiles.js";
    import { TILESET_DATA_TYPE, TILESET_SOURCE_LOCATION_TYPE } from "../utils/GameUtils.js";

    const dispatch = createEventDispatcher();

    const ZONE_DRAG = ">drag";
    const ZONE_MERGE = ">merge-";
    const ZONE_SPLIT = ">split-";

    export let tileSet;
    export let location;

    let busyDragging = false;
    let dragSelf = false;
    let showSplitZones = false;
    $: showMergeZones = busyDragging && !dragSelf;

    const unsubscribeDragging = dragging.subscribe(value => {
        busyDragging = value;
    });

    onDestroy(unsubscribeDragging);

    function eventDragStart(event, tileSetId) {
        event.dataTransfer.setData(TILESET_DATA_TYPE, tileSetId);
        event.dataTransfer.setData(TILESET_SOURCE_LOCATION_TYPE, location);
        event.dataTransfer.effectAllowed = "move";
        event.dataTransfer.dropEffect = "move";

        showSplitZones = false;
        dragging.set(true);
        dragSelf = true;
    }

    function eventDragEnd(event, tileSetId) {
        event.preventDefault();

        showSplitZones = false;
        dragging.set(false);
        dragSelf = false;
    }

    function eventDragOver(event, tileSetId) {
        event.preventDefault();
    }

    function eventHoverStart(event, tileSetId) {
        if (!dragSelf)
            showSplitZones = true;
    }
    function eventHoverEnd(event, tileSetId) {
        showSplitZones = false;
    }

    function eventDropMerge(event, tileSetId, index) {
        if (event.dataTransfer.types.includes(TILESET_DATA_TYPE)) {
            event.preventDefault();
            event.stopPropagation();

            let sourceId = event.dataTransfer.getData(TILESET_DATA_TYPE);
            let targetId = tileSetId;

            dispatch('merge', {
                sourceId: sourceId,
                targetId: targetId,
                index: index,
                location: location
            });
        }
    }

    function eventClickSplit(event, tileSetId, index) {
        event.preventDefault();
        dispatch('split', {
            id: tileSetId,
            index: index,
            location: location
        });
    }

</script>

<div id={tileSet.id+ZONE_DRAG} draggable="true"
     on:dragstart={(event)=>eventDragStart(event, tileSet.id)}
     on:dragend={(event)=>eventDragEnd(event, tileSet.id)}
     on:mouseover={(event)=>eventHoverStart(event, tileSet.id)}
     on:mouseout={(event)=>eventHoverEnd(event, tileSet.id)}
     on:mousemove={()=>{showSplitZones = true;}}
     class:bg-red-500="{!tileSet.isValid}"
     class="relative flex-none flex items-center px-2 py-1 mx-2 my-1 z-20 hover:bg-gray-600 hover:cursor-pointer">
    {#if showMergeZones}
        <div class="merge-zones-container">
                <div id={tileSet.id+ZONE_MERGE+0} class="w-5 h-full bg-gray-400 opacity-50 z-10"
                     on:drop={(event)=>eventDropMerge(event, tileSet.id, 0)}
                     on:dragover={(event)=>eventDragOver(event, tileSet.id)}
                     in:scale="{{duration: 400, opacity: 0.5, start: 0.5, easing: quintOut}}">&nbsp;</div>
                {#each tileSet.tiles as tile, i}
                    <div id={tileSet.id+ZONE_MERGE+(i+1)} class="w-5 h-full bg-gray-400 opacity-50 z-10"
                         on:drop={(event)=>eventDropMerge(event, tileSet.id, i+1)}
                         on:dragover={(event)=>eventDragOver(event, tileSet.id)}
                         in:scale="{{duration: 400, opacity: 0.5, start: 0.5, easing: quintOut}}">&nbsp;</div>
                {/each}
        </div>
    {/if}
<!--    class:hidden="{!showMergeZones}"-->
    {#each tileSet.tiles as tile, i}
        <Tile {tile}></Tile>
    {/each}
    <div class="split-zones-container">
        {#each tileSet.tiles as tile, i}
            {#if i < (tileSet.tiles.length - 1)}
                <div id={tileSet.id+ZONE_SPLIT + (i+1)} class="w-5 h-full opacity-75 hover:bg-gray-400 hover:cursor-ew-resize"
                     class:hidden={!showSplitZones}
                     in:scale="{{duration: 400, opacity: 0.5, start: 0.5, easing: quintOut}}"
                     on:click={(event)=>eventClickSplit(event, tileSet.id, i+1)}>
                    &nbsp;
                </div>
            {/if}
        {/each}
    </div>
</div>

<style>
    .split-zones-container {
        @apply absolute h-full w-full px-4 flex flex-none justify-evenly;
        left:0px;
        top:0px;
    }
    .merge-zones-container {
        @apply absolute h-full w-full flex flex-none justify-between;
        left:0px;
        top:0px;
    }
</style>