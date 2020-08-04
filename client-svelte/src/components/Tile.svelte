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
        // if(color === "BLACK")
        //     return "black";
        // if(color === "any")
        //     return "gray";

        // return color.toLowerCase() + "-600";
    }
</script>

<div class="w-12 h-16 border border-gray-300 rounded bg-orange-200 text-center text-2xl text-{colorToStyle(tile.color)}">
    {#if tile.isJoker}
        J
    {:else}
        {tile.number}
    {/if}
    <slot></slot>
</div>

<style></style>