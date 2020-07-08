<script>
    import { connection } from "../connection.js"
    let playerName;

    function clickOk() {
        console.log("Entered name is: " + playerName);
        const msg = connection.connect(undefined, playerName);
        console.log(msg);
    }
</script>

<div>
    <input type="text" id="playerName" placeholder="Pick a name..." bind:value={playerName}>
    <button id="okButton" on:click={clickOk}>Ok</button>
</div>