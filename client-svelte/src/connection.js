// let socket;
// let playerId;
// let playerName;

const connection = {
    socket : null,
    connect : function(url = "ws://localhost:8080/join", playerName) {
        if (window.WebSocket) {
            const join_url = new URL(url);
            join_url.searchParams.append("name", playerName);
            this.socket = new WebSocket(join_url.href);
            this.socket.onmessage = this.receive
            this.socket.onopen = function (event) {
            };
            this.socket.onclose = function (event) {
                // TODO handle server disconnect
            };
            return "Connection established.";
        } else {
            return "Your browser does not support Websockets. Which is weird nowadays.";
        }
    },
    disconnect : function() {
        if (this.socket !== false && this.socket.readyState == WebSocket.OPEN) {
            this.socket.close();
        }
    },
    send : function(message) {
        if (!window.WebSocket && this.socket !== null) {
            return;
        }
        if (this.socket.readyState == WebSocket.OPEN) {
            this.socket.send(message);
        } else {
            alert("The socket is not open.");
        }
    },
    receive : function(event) {
        let message = JSON.parse(event.data)
        let messageType = message.messageType
        switch (messageType) {
            case "MessageResponse":
                document.getElementById("messageField").textContent = message.message
                break;
            case "Connected":
                playerId = message.player.id
                playerName = message.player.name
                document.getElementById("response").value = playerName + " " + playerId
                break;
            case "GameListResponse":
                document.getElementById("messages").textContent = event.data
                break;
            case "PlayerListResponse":
                document.getElementById("messages").textContent = event.data
                break;
            default:
                break;
        }
    }
}

export { connection };

// function connect(playerName) {
//     if (window.WebSocket) {
//         socket = new WebSocket("ws://localhost:8080/join?name="+playerName);
//         socket.onmessage = function (event) {
//             let message = JSON.parse(event.data)
//             let messageType = message.messageType
//             switch (messageType) {
//                 case "MessageResponse":
//                     document.getElementById("messageField").textContent = message.message
//                     break;
//                 case "Connected":
//                     playerId = message.player.id
//                     playerName = message.player.name
//                     document.getElementById("response").value = playerName + " " + playerId
//                     break;
//                 case "GameListResponse":
//                     document.getElementById("messages").textContent = event.data
//                     break;
//                 case "PlayerListResponse":
//                     document.getElementById("messages").textContent = event.data
//                     break;
//                 default:
//                     break;
//             }
//         }
//         socket.onopen = function (event) {
//         };
//         socket.onclose = function (event) {
//             // TODO handle server disconnect
//         };
//     } else {
//         alert("Your browser does not support Websockets. Which is weird nowadays.");
//     }
// }

// function disconnect() {
//     if (socket.readyState == WebSocket.OPEN) {
//         socket.close();
//     }
// }

// function send(message) {
//     if (!window.WebSocket) {
//         return;
//     }
//     if (socket.readyState == WebSocket.OPEN) {
//         socket.send(message);
//     } else {
//         alert("The socket is not open.");
//     }
// }