let messageHandler;
let closeHandler;
let errorHandler;

class Connection {
    constructor(url, playerName, onMessageHandler, onCloseHandler, onErrorHandler) {
        messageHandler = onMessageHandler;
        closeHandler = onCloseHandler;
        errorHandler = onErrorHandler;
        this.connect(url, playerName);
    }

    connect(url = "ws://localhost:8080/join", playerName) {
        if (window.WebSocket) {
            const join_url = new URL(url);
            join_url.searchParams.append("name", playerName);
            this.socket = new WebSocket(join_url.href);
            this.socket.onmessage = this.receive;
            this.socket.onopen = function (event) {
            };
            this.socket.onclose = this.onClose;
            this.socket.onerror = this.onError;
            return "Connection established.";
        } else {
            return "Your browser does not support Websockets.";
        }
    }

    onClose(event) {
        closeHandler();
    }

    onError(event) {
        errorHandler();
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
            console.log("The socket is not open.");
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