<script>
    import Modal from "./utils/Modal.svelte";
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    let playerName;
    export let player = undefined;
    export let invalidPlayerNameError = false;

    function clickConnect() {
        dispatch('connect', {
            playerName: playerName
        });
    }
    function clickDisconnect() {
        if (player === undefined)
            return;

        dispatch('disconnect', {});
        playerName = undefined;
    }
</script>

{#if player === undefined }
    <Modal show="{true}">
        <svg slot="icon" class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
        <h3 slot="header" class="font-inter text-lg leading-6 font-medium text-gray-900" id="modal-headline">
            Welcome and please register.
        </h3>
        <div slot="message" class="mt-2">
            <p class="font-inter text-sm leading-5 text-gray-500">
                {#if invalidPlayerNameError }
                    {invalidPlayerNameError}
                {:else}
                    Please enter your name to play.
                {/if}
            </p>
        </div>
        <div slot="actions" class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button id="connectButton" disabled="{!playerName}" on:click={clickConnect} class="form-input">Join lounge</button>
            <input type="text" id="playerName" placeholder="Pick a name..." bind:value={playerName} class="form-input">
        </div>
    </Modal>
{:else}
    <div id="registerPlayer" class="inline float-right flex flex-end">
        <div class="w-16 text-white flex justify-end items-center p-1">
            <svg class="w-8" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
        </div>
        <div class="flex items-center">
            <button id="disconnectButton" on:click={clickDisconnect} class="form-input rounded-none">Leave lounge</button>
        </div>
    </div>
{/if}

