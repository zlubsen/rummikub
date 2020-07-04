<script>
    export let tile;
</script>

<span>
    {#if tile.isJoker}
        {tile.color} - Joker
    {:else}
        {tile.color} - {tile.number}
    {/if}
</span>

<style></style>