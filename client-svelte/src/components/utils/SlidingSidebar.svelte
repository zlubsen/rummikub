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

function pin(event) {
    auto_minimize = false;
    event.stopPropagation();
}
function unpin(event) {
    auto_minimize = true;
    event.stopPropagation();
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
    transform translate-x-2 z-20
    flex flex-col items-stretch"
    transition:fade={{delay: 250, duration: 300, easing: quintOut }}
    on:mouseleave={sidebarMouseLeave}
    on:mouseenter={sidebarMouseEnter}>
    <header class="h-8 self-end font-inter text-inter-base text-orange-500" on:click={minimize}>
        {#if auto_minimize}
            <span class="text-right align-text-top cursor-pointer" on:click={pin}>
                Pin&nbsp;
<!--                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">-->
<!--                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13l-3 3m0 0l-3-3m3 3V8m0 13a9 9 0 110-18 9 9 0 010 18z" />-->
<!--                </svg>-->
            </span>
        {:else}
            <span class="text-right align-text-top cursor-pointer" on:click={unpin}>
                Unpin&nbsp;
<!--                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">-->
<!--                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 11l3-3m0 0l3 3m-3-3v8m0-13a9 9 0 110 18 9 9 0 010-18z" />-->
<!--                </svg>-->
            </span>
        {/if}
        <span class="text-right align-text-top cursor-pointer" on:click={minimize}>
            Close&nbsp;
<!--            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">-->
<!--                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z" />-->
<!--            </svg>-->
        </span>
    </header>
    <slot name="content">There should be some content here...</slot>
</div>
{/if}
<slot></slot>

<style>
    .label-invisible {
        @apply translate-x-24;
    }
</style>