<script>
    import TileSet from "./TileSet.svelte";

    // let hand = [
    //     {id:1},{id:2},{id:3},{id:4},{id:5},{id:6},{id:7},{id:8},{id:9}
    // ];
    let hand = [
        {
            id: "xxsdf1",
            tiles : [
                {color : "black", number : "8", isJoker: false},
                {color : "black", number : "9", isJoker: false},
                {color : "black", number : "10", isJoker: false}
            ]
        },
        {
            id: "ldjfb5",
            tiles : [
                {color : "blue", number : "1", isJoker: false},
                {color : "yellow", number : "1", isJoker: false},
                {color : "black", number : "1", isJoker: false}
            ]
        },
        {
            id: "sdlkg4",
            tiles : [
                {color : "blue", number : "8", isJoker: false},
                {color : "black", number : "3", isJoker: false},
                {color : "red", number : "10", isJoker: false},
                {color : "red", number : "10", isJoker: true}
            ]
        }
    ];
</script>

<div>
    {#each hand as tileSet}
        <TileSet {tileSet}></TileSet>
    {/each}
</div>

<style></style>