<script>
    import Tile from "./Tile.svelte";

    export let tileSet;
</script>

<div class="tileSet">
    <span>(mergeL)</span>
    {#each tileSet.tiles as tile, i}
        <Tile {tile}></Tile>
        {#if i < (tileSet.tiles.length - 1)}
            <span>-Split:{i}-</span>
        {/if}
    {/each}
    <span>(mergeR)</span>
</div>

<style>
    span:hover {
        color:yellow;
        padding: 5px;
        border: 1px solid black;
    }

    .tileSet:hover {
        border: 1px solid gray;
    }
</style>