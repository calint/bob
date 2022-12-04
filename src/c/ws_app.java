package c;

import java.nio.ByteBuffer;
import java.util.Map;
import b.a;
import b.threadedsock;
import b.websock;
import b.xwriter;

final public class ws_app extends websock implements threadedsock{
	private a root=new a();

	synchronized final @Override protected void on_opened(final Map<String,String> headers) throws Throwable{
		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_opened");
		root.set("hello world");
	}

	@Override protected void on_message(ByteBuffer bb) throws Throwable{
		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_message size:"+bb.remaining());
		final xwriter x=new xwriter();
		root.to(x);
		send(x.toString());
	}

	synchronized final @Override protected void on_closed() throws Throwable{
		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_closed");
	}
}
