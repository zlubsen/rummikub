let messageHandler;

class Connection {
    constructor(url, playerName, handler) {
        // this.handler = handler;
        messageHandler = handler;
        this.connect(url, playerName);
        // console.log(typeof this.handler);
    }

    // TODO callback ServerMessageHandler for handling received messages
    // TODO callback MessageHandler for error/success reporting

    connect(url = "ws://localhost:8080/join", playerName) {
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
    }

    disconnect() {
        if (this.socket !== false && this.socket.readyState == WebSocket.OPEN) {
            this.socket.close();
        }
    }

    send(message) {
        if (!window.WebSocket && this.socket !== null) {
            return;
        }
        if (this.socket.readyState == WebSocket.OPEN) {
            this.socket.send(message);
        } else {
            alert("The socket is not open.");
        }
    }

    sendJson(message) {
        this.send(JSON.stringify(message));
    }

    receive(event) {
        let message = JSON.parse(event.data)
        messageHandler(message);
    }
}

export { Connection };