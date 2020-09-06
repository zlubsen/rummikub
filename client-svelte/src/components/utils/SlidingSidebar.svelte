<script>
import { fade } from 'svelte/transition';
import { quintOut } from 'svelte/easing';

const MINIMIZE_TIMEOUT = 2000;
const MAXIMIZE_TIMEOUT = 1000;

let minimized = false;
export let auto_minimize = false;

let sidebarTimer;
let labelTimer;

function minimize(event) {
    minimized = true;
}

function maximize(event) {
    minimized = false;
    if(labelTimer) {
        clearTimeout(labelTimer);
        labelTimer = undefined;
    }
}

function sidebarMouseLeave(event) {
    if(auto_minimize)
        sidebarTimer = setTimeout(()=>{minimize(event)}, MINIMIZE_TIMEOUT);
}

function sidebarMouseEnter(event) {
    if(auto_minimize && sidebarTimer) {
        clearTimeout(sidebarTimer);
        sidebarTimer = undefined;
    }
}

function labelMouseLeave(event) {
    if (labelTimer){
        clearTimeout(labelTimer);
        labelTimer = undefined;
    }
}

function labelMouseEnter(event) {
    labelTimer = setTimeout(()=>{maximize(event)}, MAXIMIZE_TIMEOUT);
}

</script>

<div id="minified-label" class="absolute top-0 right-0 bg-orange-500 px-4 py-2 h-16 w-24 z-20
    text-center font-inter
    transform -rotate-90 translate-x-12 translate-y-4 hover:transition ease-in duration-200 hover:translate-x-5"
    class:label-invisible="{!minimized}"
    on:mouseleave={labelMouseLeave}
    on:mouseenter={labelMouseEnter}
    on:click={maximize}>
        <slot name="label">No label</slot>
</div>

{#if !minimized }
<div id="sidebar" class="absolute top-0 right-0 bg-blue-600 w-64 h-full
    transform translate-x-2 z-20"
    transition:fade={{delay: 250, duration: 300, easing: quintOut }}
    on:mouseleave={sidebarMouseLeave}
    on:mouseenter={sidebarMouseEnter}>
    <div class="absolute top-0 right-0 font-inter text-inter-3xl text-right align-text-top text-orange-500 cursor-pointer transform -translate-x-4 -translate-y-2" on:click={minimize}>
        &minus;
    </div>
    <div class="h-6 w-4"></div>
    <slot name="content">There should be some content here...</slot>
</div>
{/if}
<slot></slot>

<style>
    .label-invisible {
        @apply translate-x-24;
    }
</style>