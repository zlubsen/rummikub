<script>
    export let tile;

    function colorToStyle(color) {
        if(color === "BLACK")
            return "black";
        if(color === "any")
            return "white";

        return color.toLowerCase() + "-400";
    }
</script>

<div class="inline min-w-10 w-12 h-16 rounded bg-{colorToStyle(tile.color)} text-center text-2xl">
    {#if tile.isJoker}
        J
    {:else}
        {tile.number}
    {/if}
</div>

<style></style>