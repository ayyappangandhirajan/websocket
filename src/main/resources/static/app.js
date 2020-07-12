var ws;
function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
}

function connect() {
	ws = new WebSocket('ws://localhost:8080/troubleticket');
	ws.onmessage = function(data) {
		handle(data.data);
	}
	setConnected(true);
}

function handle(message) {
	$("#message").append("<tr><td> " + message + "</td></tr>");
}

function disconnect() {
	if (ws != null) {
		ws.close();
	}
	$("#message").append("<tr><td> disconnected </td></tr>");
	$("#message").empty();
	setConnected(false);
	console.log("Websocket is in disconnected state");
}

function sendData() {
	var data = JSON.stringify({
		'msg' : $("#msg").val()
	})
	ws.send(data);
}



$(function() {
	$("form").on('submit', function(e) {
		e.preventDefault();
	});
	$("#connect").click(function() {
		connect();
	});
	$("#disconnect").click(function() {
		disconnect();
	});
	$("#send").click(function() {
		sendData();
	});
});
