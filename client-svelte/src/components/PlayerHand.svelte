<script>
    import TileSet from "./TileSet.svelte";

    // let hand = [
    //     {id:1},{id:2},{id:3},{id:4},{id:5},{id:6},{id:7},{id:8},{id:9}
    // ];
    export let hand;
</script>

<div>
    {#each hand as tileSet}
<!--        <span class="text-base text-gray-600 leading-normal">{tileSet.id}</span>-->
        <TileSet {tileSet}></TileSet>
    {/each}
</div>

<style></style>