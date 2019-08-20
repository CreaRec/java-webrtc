package by.crearec.webrtc.sample.util;

import by.crearec.webrtc.sample.model.Message;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Converts the message class to a string.
 */
public class MessageEncoder implements Encoder.Text<Message> {

	@Override
	public String encode(Message message) throws EncodeException {
		return message.getJson().toString();
	}

	@Override
	public void init(EndpointConfig config) {
		System.out.println("Init");
	}

	@Override
	public void destroy() {
		System.out.println("destroy");
	}

}