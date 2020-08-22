<script>
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

<div id="registerPlayer" class="inline float-right">
    {#if playerId === undefined }
        <input type="text" id="playerName" placeholder="Pick a name..." bind:value={playerName} class="form-input">
        <button id="connectButton" disabled="{!playerName}" on:click={clickConnect} class="form-input">Join lounge</button>
    {:else}
        <button id="disconnectButton" on:click={clickDisconnect} class="form-input">Leave lounge</button>
    {/if}
</div>