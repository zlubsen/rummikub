<script>
    import { createEventDispatcher, onDestroy } from 'svelte';
    import { fade, scale } from 'svelte/transition';
    import { quintOut } from 'svelte/easing';
    import Tile from "./Tile.svelte";
    import { dragging } from "../stores/dragTiles.js";
    import { TILESET_DATA_TYPE, TILESET_SOURCE_LOCATION_TYPE } from "../utils/GameUtils.js";

    const dispatch = createEventDispatcher();

    const LEFT = 'left';
    const RIGHT = 'right';
    const ZONE_DRAG = ">drag";
    const ZONE_MERGE = ">merge-";
    const ZONE_MERGE_LEFT = ZONE_MERGE + LEFT;
    const ZONE_MERGE_RIGHT = ZONE_MERGE + RIGHT;
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

    // function showMergeZones() {
    //     dragging.set(true);
    //     dragSelf = true;
    //     // let elements = document.querySelectorAll("[id*='"+ZONE_MERGE+"'");
    //     // elements.forEach((element)=>element.style.display = "inline");
    // }
    //
    // function hideMergeZones() {
    //     dragging.set(false);
    //     dragSelf = false;
    //     // let elements = document.querySelectorAll("[id*='"+ZONE_MERGE+"'");
    //     // elements.forEach((element)=>element.style.display = "none");
    // }

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

    function eventDropMerge(event, tileSetId, side) {
        if (event.dataTransfer.types.includes(TILESET_DATA_TYPE)) {
            event.preventDefault();
            event.stopPropagation();

            let leftId;
            let rightId;
            if(side==='left') {
                leftId = event.dataTransfer.getData(TILESET_DATA_TYPE);
                rightId = tileSetId;
            } else {
                leftId = tileSetId;
                rightId = event.dataTransfer.getData(TILESET_DATA_TYPE);
            }

            dispatch('merge', {
                leftId: leftId,
                rightId: rightId,
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
        console.log("eventClicksplit");
    }

</script>

<div id={tileSet.id+ZONE_DRAG} draggable="true"
     on:dragstart={(event)=>eventDragStart(event, tileSet.id)}
     on:dragend={(event)=>eventDragEnd(event, tileSet.id)}
     on:mouseover={(event)=>eventHoverStart(event, tileSet.id)}
     on:mouseout={(event)=>eventHoverEnd(event, tileSet.id)}
     on:mousemove={()=>{showSplitZones = true;}}
     class:bg-red-100="{!tileSet.isValid}"
     class="relative flex-none flex px-2 py-1 mx-2 my-1 z-20">
    {#if showMergeZones }
        <div id={tileSet.id+ZONE_MERGE_LEFT} class="merge-zone-l"
             on:drop={(event)=>eventDropMerge(event, tileSet.id, 'left')}
             on:dragover={(event)=>eventDragOver(event, tileSet.id)}>&nbsp;</div>
        <div id={tileSet.id+ZONE_MERGE_RIGHT} class="merge-zone-r"
             on:drop={(event)=>eventDropMerge(event, tileSet.id, 'right')}
             on:dragover={(event)=>eventDragOver(event, tileSet.id)}>&nbsp;</div>
    {/if}
    {#each tileSet.tiles as tile, i}
        <Tile {tile}></Tile>
    {/each}
    <div class="split-zones-container px-4">
        {#each tileSet.tiles as tile, i}
            {#if i < (tileSet.tiles.length - 1)}
                <div id={tileSet.id+ZONE_SPLIT + (i+1)} class="w-5 h-full hover:bg-gray-400 z-10"
                     class:hidden="{!showSplitZones}"
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
        @apply absolute h-full w-full flex flex-none justify-evenly;
        left:0px;
        top:0px;
    }
    .merge-zone-l {
        @apply absolute h-full w-5 px-1 border bg-gray-400 opacity-50 z-10;
        left:0px;
        top:0px;
    }
    .merge-zone-r {
        @apply absolute h-full w-5 px-1 border bg-gray-400 opacity-50 z-10;
        right:0px;
        top:0px;
    }
</style>

<!--flex flex-no-wrap items-start-->