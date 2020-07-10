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
        dispatch('disconnect', {});
        playerName = undefined;
    }
</script>

<div>
    {#if playerId === undefined }
        <input type="text" id="playerName" placeholder="Pick a name..." bind:value={playerName}>
        <button id="connectButton" on:click={clickConnect}>Join lounge</button>
    {:else}
        <div>{playerId} - {playerName}</div>
        <button id="disconnectButton" on:click={clickDisconnect}>Leave lounge</button>
    {/if}
</div>