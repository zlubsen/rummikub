<script>
    import Modal from "./Modal.svelte";
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    let playerName;
    export let playerId;

    function clickConnect() {
        dispatch('connect', {
            playerName: playerName
        });
    }
    function clickDisconnect() {
        if (playerId === undefined)
            return;

        dispatch('disconnect', {});
        playerName = undefined;
    }
</script>

{#if playerId === undefined }
    <Modal show="{true}">
        <svg slot="icon" class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
        <h3 slot="header" class="font-inter text-lg leading-6 font-medium text-gray-900" id="modal-headline">
            Welcome and please register.
        </h3>
        <div slot="message" class="mt-2">
            <p class="font-inter text-sm leading-5 text-gray-500">
                Please enter your name to play.
            </p>
        </div>
        <div slot="actions" class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button id="connectButton" disabled="{!playerName}" on:click={clickConnect} class="form-input">Join lounge</button>
            <input type="text" id="playerName" placeholder="Pick a name..." bind:value={playerName} class="form-input">
        </div>
    </Modal>
{/if}

<div id="registerPlayer" class="inline float-right">
    {#if playerId !== undefined }
        <button id="disconnectButton" on:click={clickDisconnect} class="form-input">Leave lounge</button>
    {/if}
</div>