var socket = new WebSocket('ws://' + window.location.host + room);

socket.onmessage = function(message) {
	var msg = JSON.parse(message.data);

	switch (msg.type) {
		case 'assigned_id':
			socket.id = msg.id;
			break;
		case 'received_offer':
			console.log('received offer', msg.data);
			pc.setRemoteDescription(new RTCSessionDescription(msg.data));
			pc.createAnswer(mediaConstraints)
					.then(function(answer) {
						socket.send(JSON.stringify({
							type: 'received_answer',
							data: answer
						}));
						return pc.setLocalDescription(answer);
					})
					.catch(handleError);
			break;
		case 'received_answer':
			console.log('received answer');
			if (!connected) {
				pc.setRemoteDescription(new RTCSessionDescription(msg.data));
				connected = true;
			}
			break;
		case 'received_candidate':
			console.log('received candidate');
			var candidate = new RTCIceCandidate({
				sdpMLineIndex: msg.data.label,
				candidate: msg.data.candidate
			});
			pc.addIceCandidate(candidate);
			break;
		case 'connection_closed':
			console.log('peer ' + msg.peer + ' closed connection');
			break;
	}
};

var configuration = {
	"iceServers": [{
		"url": "stun:stun.l.google.com:19302"
	}]
};
var stream;
var pc = new webkitRTCPeerConnection(configuration);
var connected = false;
var mediaConstraints = {
	'mandatory': {
		'OfferToReceiveAudio': true,
		'OfferToReceiveVideo': true
	}
};

pc.onicecandidate = function(e) {
	if (e.candidate) {
		socket.send(JSON.stringify({
			type: 'received_candidate',
			data: {
				label: e.candidate.sdpMLineIndex,
				id: e.candidate.sdpMid,
				candidate: e.candidate.candidate
			}
		}));
	}
};

pc.onaddstream = function(e) {
	console.log('start remote video stream');
	vid2.srcObject = e.stream;
	vid2.play();
};

function broadcast() {
	// gets local video stream and renders to vid1
	navigator.webkitGetUserMedia({
		audio: true,
		video: true
	}, function(s) {
		stream = s;
		pc.addStream(s);
		vid1.srcObject = s;
		vid1.play();
		// initCall is set in views/index and is based on if there is another
		// person in the room to connect to
		if (initCall) {
			start();
		}
	}, function(error) {
		try {
			console.error(error);
		} catch (e) {
		}
	});
}

function start() {
	pc.createOffer(mediaConstraints)
			.then(function(answer) {
				socket.send(JSON.stringify({
					type: 'received_offer',
					data: answer
				}));
				return pc.setLocalDescription(answer);
			})
			.catch(handleError);
}

function handleError() {
}

window.onload = function() {
	broadcast();
};

window.onbeforeunload = function() {
	socket.send(JSON.stringify({
		type: 'close'
	}));
	pc.close();
	pc = null;
};

function getParameterByName(name) {
	name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
	var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"), results = regex.exec(location.search);
	return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}