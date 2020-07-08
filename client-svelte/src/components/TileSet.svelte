<script>
    import Tile from "./Tile.svelte";

    export let tileSet;
</script>

<div class="inline flex flex-row border p-2 max-w-sm hover:border-black">
    <div class="max-w-10 border-1 border p-2">&nbsp;</div>
    {#each tileSet.tiles as tile, i}
        <Tile {tile}></Tile>
        {#if i < (tileSet.tiles.length - 1)}
            <div class="max-w-10 border-1 border p-2 hover:bg-gray-200">&nbsp;</div>
        {/if}
    {/each}
    <div class="max-w-10 border-1 border p-2">&nbsp;</div>
</div>

<!--<style>-->
<!--    span:hover {-->
<!--        color:yellow;-->
<!--        padding: 5px;-->
<!--        border: 1px solid black;-->
<!--    }-->

<!--    .tileSet:hover {-->
<!--        border: 1px solid gray;-->
<!--    }-->
<!--</style>-->