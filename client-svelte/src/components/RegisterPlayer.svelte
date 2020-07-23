<script>
    import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();

    let playerName;
    export let playerId;

    function clickConnect() {
        if (playerName === undefined)
            return;

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
        <input type="text" id="playerName" placeholder="Pick a name..." bind:value={playerName}>
        <button id="connectButton" on:click={clickConnect}>Join lounge</button>
    {:else}
        <button id="disconnectButton" on:click={clickDisconnect}>Leave lounge</button>
    {/if}
</div>