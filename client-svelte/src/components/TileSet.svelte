<script>
    import Tile from "./Tile.svelte";
    import { createEventDispatcher } from 'svelte';
    import { TILESET_DATA_TYPE, TILESET_SOURCE_LOCATION_TYPE } from "../utils/GameUtils.js";

    const dispatch = createEventDispatcher();

    const ZONE_DRAG = ">drag";
    const ZONE_MERGE = ">merge-";
    const ZONE_MERGE_LEFT = ZONE_MERGE + "left";
    const ZONE_MERGE_RIGHT = ZONE_MERGE + "right";
    const ZONE_SPLIT = ">split-";

    export let tileSet;
    export let location;

    function showMergeZones() {
        let elements = document.querySelectorAll("[id*='"+ZONE_MERGE+"'");
        elements.forEach((element)=>element.style.display = "inline");
    }
    function hideMergeZones() {
        let elements = document.querySelectorAll("[id*='"+ZONE_MERGE+"");
        elements.forEach((element)=>element.style.display = "none");
    }

    function eventDragStart(event, tileSetId) {
        console.log("eventDragStart: " + tileSetId);
        event.dataTransfer.setData(TILESET_DATA_TYPE, tileSetId);
        event.dataTransfer.setData(TILESET_SOURCE_LOCATION_TYPE, location);
        event.dataTransfer.effectAllowed = "move";
        event.dataTransfer.dropEffect = "move";
        showMergeZones();
    }

    function eventDragEnd(event, tileSetId) {
        console.log("eventDragEnd: " + tileSetId);
        hideMergeZones();
    }

    function eventDragOver(event, tileSetId) {
        console.log("eventDragOver: " + tileSetId);
        console.log("eventDragOver onto: " + event.target);
        event.preventDefault();
    }

    function eventHoverStart(event, tileSetId) {
        let elements = document.querySelectorAll("[id^='"+tileSetId+ZONE_SPLIT+"'");
        elements.forEach((element)=>element.style.display = "inline");
    }
    function eventHoverEnd(event, tileSetId) {
        let elements = document.querySelectorAll("[id^='"+tileSetId+ZONE_SPLIT+"'");
        elements.forEach((element)=>element.style.display = "none");
    }

    function eventDropMerge(event, tileSetId, side) {
        console.log("eventDropMerge: " + tileSetId);
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
        hideMergeZones();
    }

    function eventClickSplit(event, tileSetId, index) {
        console.log("eventClickSplit: " + tileSetId);
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
     class:bg-red-200={!tileSet.isValid}
     class="inline-flex border p-2 hover:border-black">
    <div id={tileSet.id+ZONE_MERGE_LEFT}
         on:drop={(event)=>eventDropMerge(event, tileSet.id, 'left')}
         on:dragover={(event)=>eventDragOver(event, tileSet.id)}
         class="hidden max-w-10 border p-2 hover:bg-blue-100">&nbsp;</div>
    {#each tileSet.tiles as tile, i}
        <Tile {tile}></Tile>
        {#if i < (tileSet.tiles.length - 1)}
            <div id={tileSet.id+ZONE_SPLIT + (i+1)}
                 on:click={(event)=>eventClickSplit(event, tileSet.id, i+1)}
                 class="hidden max-w-10 border-1 border p-2 hover:bg-gray-200">&nbsp;</div>
        {/if}
    {/each}
    <div id={tileSet.id+ZONE_MERGE_RIGHT}
         on:drop={(event)=>eventDropMerge(event, tileSet.id, 'right')}
         on:dragover={(event)=>eventDragOver(event, tileSet.id)}
         class="hidden max-w-10 border-1 border p-2">&nbsp;</div>
</div>