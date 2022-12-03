package c;

import java.nio.ByteBuffer;
import java.util.Map;

import b.threadedsock;
import b.websock;

final public class ws_app extends websock implements threadedsock {

	synchronized final @Override protected void on_opened(final Map<String, String> headers) throws Throwable {
		System.out.println("websocket " + Integer.toHexString(hashCode()) + ": on_opened");
	}

	@Override
	protected void on_message(ByteBuffer bb) throws Throwable {
		System.out.println("websocket " + Integer.toHexString(hashCode()) + ": on_message size:" + bb.remaining());
		String msg = new String(bb.array(), bb.position(), bb.remaining());
		send(msg);
	}

	synchronized final @Override protected void on_closed() throws Throwable {
		System.out.println("websocket " + Integer.toHexString(hashCode()) + ": on_closed");
	}
}
