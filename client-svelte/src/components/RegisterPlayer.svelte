<script>
    let playerName;

    function clickOk() {
        console.log("Entered name is: " + playerName);
    }
</script>

<div>
    <input type="text" id="playerName" placeholder="Pick a name..." bind:value={playerName}>
    <button id="okButton" on:click={clickOk}>Ok</button>
</div>