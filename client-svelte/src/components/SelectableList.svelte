<script>
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    export let items;
    export let selectedItem;

function selectItem(event, item) {
    event.stopPropagation();
    selectedItem = item;
    dispatch('select');
}

function unselect(event) {
    selectedItem = null;
}

</script>

<div class="bg-blue-500 p-1 overflow-y-scroll">
        <ul class="h-56" on:click={unselect}>
            {#each items as item}
                <li class="list-none p-0 font-inter text-base text-white"
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