package c;

import java.nio.ByteBuffer;
import b.websock;

final public class websocket extends websock{
	public websocket(){
		super(true);
	}

	synchronized final @Override protected void on_opened() throws Throwable{
		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_opened");
	}

	@Override protected void on_message(ByteBuffer bb) throws Throwable{
		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_message size:"+bb.remaining());
		String msg=new String(bb.array(),bb.position(),bb.remaining());
		send(msg);
	}

	synchronized final @Override protected void on_closed() throws Throwable{
		System.out.println("websocket "+Integer.toHexString(hashCode())+": on_closed");
	}
}
