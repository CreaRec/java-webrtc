package by.crearec.webrtc.sample.socket;

import static java.util.Collections.emptySet;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import by.crearec.webrtc.sample.Room;
import by.crearec.webrtc.sample.util.MessageDecoder;
import by.crearec.webrtc.sample.util.MessageEncoder;
import by.crearec.webrtc.sample.model.Message;

@ServerEndpoint(value = "/server/{room}", encoders = {MessageEncoder.class}, decoders = {MessageDecoder.class})
public class WebSocketListener {

    private ConcurrentMap<String, Set<Session>> rooms = Room.INSTANCE.map();

    private static final Set<Session> EMPTY_ROOM = emptySet();

    @OnOpen
    public void onOpen(Session peer, @PathParam("room") String room) throws IOException {
	    System.out.println("onOpen room " + room);
        if (room != null && !room.isEmpty()) {        	
        	rooms.computeIfAbsent(room, s -> new CopyOnWriteArraySet<>()).add(peer);
			peer.getBasicRemote().sendText(Json.createObjectBuilder().add("type", "assigned_id").add("id", room).build().toString());
        } else {
			peer.close();
        }
        
    }

    @OnClose
    public void onClose(Session peer, @PathParam("room") String room) throws IOException {
	    System.out.println("onClose room " + room);
        Optional.ofNullable(rooms.get(room))
                .orElseThrow(() -> new IllegalStateException("Cannot find room " + room))
                .remove(peer);
        rooms.getOrDefault(room, EMPTY_ROOM)
        	.parallelStream()
        	.filter(s -> s.isOpen())
        	.forEach(s -> s.getAsyncRemote()
        			.sendText(Json.createObjectBuilder().add("type", "connection_closed").add("peer", peer.getId()).build().toString()));
    }

    @OnError
    public void onError(Session peer, Throwable th, @PathParam("room") String room) {
        System.out.println("Peer error " + room + " " + th);
    }

    @OnMessage
    public void onMessage(Message message, Session peer, @PathParam("room") String room) throws IOException {
	    System.out.println("onMessage room " + room + " message " + message);
        JsonObject o = message.getJson();
        String type = o.getString("type");
	    System.out.println("onMessage type " + type);
        switch(type) {
        case "received_offer":
        case "received_candidate":
        case "received_answer":
        	rooms.getOrDefault(room, EMPTY_ROOM).parallelStream()
		    	.filter(s -> s != peer && s.isOpen())
		    	.forEach(s -> s.getAsyncRemote().sendObject(message));
        	break;
        case "close":
        	peer.close();
        }
    }
}
