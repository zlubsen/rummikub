<script>
    import TileSet from "./TileSet.svelte";
    import { LOCATION_HAND, TILESET_SOURCE_LOCATION_TYPE, createMoveTilesEvent } from "../utils/GameUtils.js";
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    export let hand;

    function eventMoveTilesToHand(event) {
        console.log("eventMoveTilesToHand");
        if (event.dataTransfer.getData(TILESET_SOURCE_LOCATION_TYPE)!==LOCATION_HAND) {
            const payload = createMoveTilesEvent(event, LOCATION_HAND);
            dispatch(payload.eventName, payload);
        }
    }

    function eventDragOver(event) {
        event.preventDefault();
    }

</script>

<div id="playerHand" class="w-3/4 h-2/5 border border-blue-300"
    on:drop={eventMoveTilesToHand}
     on:dragover={eventDragOver}>
    {#each [...hand] as [id, tileSet]}
        <TileSet {tileSet} location="HAND" on:merge on:split></TileSet>
    {/each}
</div>

<style></style>