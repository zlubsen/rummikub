let samplePlayers = new Map();
let sampleGames = new Map();
let sampleTable = new Map();
let sampleHand = new Map();

sampleGames.set("Aap",
    {
        gameName: "Aap",
        gameState:"STARTED"
    });
sampleGames.set("Noot",
    {
        gameName: "Noot",
        gameState:"STARTED"
    });
sampleGames.set("Mies",
    {
        gameName: "Mies",
        gameState:"STARTED"
    });

samplePlayers.set("p1", {id:"p1", name:"MyPlayer"});
samplePlayers.set("p2", {id:"p2", name:"OtherPlayer"});
samplePlayers.set("p3", {id:"p3", name:"AnotherPlayer"});

sampleTable.set("xxsdf1",
    {
        id: "xxsdf1",
        tiles: [
            {color: "BLACK", number: "8", isJoker: false},
            {color: "BLACK", number: "9", isJoker: false},
            {color: "BLACK", number: "10", isJoker: false}
        ],
        isValid: true
    });
sampleTable.set("ldjfb5",
    {
        id: "ldjfb5",
        tiles: [
            {color: "BLUE", number: "1", isJoker: false},
            {color: "YELLOW", number: "1", isJoker: false},
            {color: "BLACK", number: "1", isJoker: false}
        ],
        isValid: true
    });
sampleTable.set("sdlkg4",
    {
        id: "sdlkg4",
        tiles: [
            {color: "BLUE", number: "8", isJoker: false},
            {color: "BLACK", number: "3", isJoker: false},
            {color: "RED", number: "10", isJoker: false},
            {color: "RED", number: "10", isJoker: true}
        ],
        isValid: false
    });
sampleTable.set("livnr5",
    {
        id: "livnr5",
        tiles: [
            {color: "BLUE", number: "6", isJoker: false},
            {color: "BLUE", number: "7", isJoker: false},
            {color: "BLUE", number: "8", isJoker: false},
            {color: "BLUE", number: "9", isJoker: false},
            {color: "BLUE", number: "10", isJoker: false},
            {color: "BLUE", number: "11", isJoker: false},
            {color: "BLUE", number: "12", isJoker: false},
            {color: "BLUE", number: "13", isJoker: false},
        ],
        isValid: true
    });

sampleHand.set("xxsdf1",
    {
        id: "xxsdf1",
        tiles: [
            {color: "BLACK", number: "8", isJoker: false},
            {color: "BLACK", number: "9", isJoker: false},
            {color: "BLACK", number: "10", isJoker: false}
        ],
        isValid: true
    });
sampleHand.set("ldjfb5",
    {
        id: "ldjfb5",
        tiles: [
            {color: "BLUE", number: "1", isJoker: false},
            {color: "YELLOW", number: "1", isJoker: false},
            {color: "BLACK", number: "1", isJoker: false}
        ],
        isValid: true
    });
sampleHand.set("sdlkg4",
    {
        id: "sdlkg4",
        tiles: [
            {color: "BLUE", number: "8", isJoker: false},
            {color: "BLACK", number: "3", isJoker: false},
            {color: "RED", number: "10", isJoker: false},
            {color: "RED", number: "10", isJoker: true}
        ],
        isValid: false
    });
sampleHand.set("livnr5",
    {
        id: "livnr5",
        tiles: [
            {color: "BLUE", number: "6", isJoker: false},
            {color: "BLUE", number: "7", isJoker: false},
            {color: "BLUE", number: "8", isJoker: false},
            {color: "BLUE", number: "9", isJoker: false},
            {color: "BLUE", number: "10", isJoker: false},
            {color: "BLUE", number: "11", isJoker: false},
            {color: "BLUE", number: "12", isJoker: false},
            {color: "BLUE", number: "13", isJoker: false},
        ],
        isValid: true
    });

let samplePlayer = samplePlayers.get("p1");
let sampleCurrentGame = "Aap";
let sampleCurrentPlayer = "p1";
let samplePlayersInCurrentGame = [{id:"p1", name:"MyPlayer"}, {id:"p2", name:"OtherPlayer"}, {id:"p3", name:"AnotherPlayer"}];

let sampleMessage = [];
sampleMessage.push("Some message");
sampleMessage.push("More text");
sampleMessage.push("Very insightful, actually");
sampleMessage.push("It just keeps on going...");

export {
    samplePlayers,
    sampleGames,
    sampleHand,
    sampleTable,
    samplePlayer,
    sampleCurrentGame,
    sampleCurrentPlayer,
    samplePlayersInCurrentGame,
    sampleMessage
}