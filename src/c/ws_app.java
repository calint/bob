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
		final xwriter x=new xwriter();
		x.xub(root,true,false);
		root.to(x);
		x.xube();
		send(x.toString());
	}

	@Override protected void on_message(ByteBuffer bb) throws Throwable{
		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_message size:"+bb.remaining());
		send(bb,true);
	}

	synchronized final @Override protected void on_closed() throws Throwable{
		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_closed");
	}
}
