<script>
    export let tile;

    function colorToStyle(color) {
        switch(color) {
            case "BLACK":
                return "black";
            case "YELLOW":
                return "yellow-600";
            case "BLUE":
                return "blue-600";
            case "RED":
                return "red-600";
            default:
                return "gray-600";
        }
    }
</script>

<div class="w-12 h-16 border border-gray-300 rounded bg-orange-200 text-center text-2xl font-bold text-{colorToStyle(tile.color)}">
    {#if tile.isJoker}
        &#129313;
<!--        TODO: nice icon for the joker (aligning SVG image was a pain) -->
<!--        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 16 16" preserveAspectRatio="xMaxYMax meet" stroke="currentColor">-->
<!--            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />-->
<!--        </svg>-->
    {:else}
        {tile.number}
    {/if}
</div>

<style></style>