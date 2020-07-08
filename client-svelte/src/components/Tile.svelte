<script>
    export let tile;
</script>

<div class="inline min-w-10 w-12 h-16 rounded bg-{tile.color}-400 text-center text-2xl">
    {#if tile.isJoker}
        &#129313;
    {:else}
        {tile.number}
    {/if}
</div>

<style></style>