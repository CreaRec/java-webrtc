package by.crearec.webrtc.sample.util;

import by.crearec.webrtc.sample.model.Message;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;

/**
 * Decodes a client-sent string into a Message class
 */
public class MessageDecoder implements Decoder.Text<Message> {
	/**
	 * Transform the input string into a Message
	 */
	@Override
	public Message decode(String string) throws DecodeException {
		JsonObject json = Json.createReader(new StringReader(string)).readObject();
		return new Message(json);
	}

	/**
	 * Checks whether the input can be turned into a valid Message object
	 * in this case, if we can read it as a Json object, we can.
	 */
	@Override
	public boolean willDecode(String string) {
		try {
			Json.createReader(new StringReader(string)).read();
			return true;
		} catch (JsonException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	/**
	 * The following two methods are placeholders as we don't need to do anything
	 * special for init or destroy.
	 */
	@Override
	public void init(EndpointConfig config) {
		System.out.println("init");
	}

	@Override
	public void destroy() {
		System.out.println("destroy");
	}

}