<script>

export let items;
export let selectedItem;

function selectItem(event, item) {
    event.stopPropagation();
    selectedItem = item;
}
function unselect(event) {
    selectedItem = undefined;
}

</script>

<div class="bg-gray-100 p-2 overflow-y-scroll">
        <ul class="border h-56" on:click={unselect}>
            {#each items as item}
                <li class="list-none p-1 font-inter text-base"
                class:selected={item.gameName == selectedItem}
                on:click={(event)=>selectItem(event, item.gameName)}>
                    {item.gameName}
                </li>
            {/each}
        </ul>
</div>

<style>
    .selected {
        @apply bg-blue-200 border;
    }
</style>