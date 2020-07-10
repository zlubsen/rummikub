<script>
    import TileSet from "./TileSet.svelte";

    export let hand;
</script>

<div>
    {#each hand as tileSet}
        <TileSet {tileSet}></TileSet>
    {/each}
</div>

<style></style>