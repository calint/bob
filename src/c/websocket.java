package c;

import java.nio.ByteBuffer;

import b.threadedsock;
import b.websock;

final public class websocket extends websock implements threadedsock {

	synchronized final @Override protected void on_opened() throws Throwable {
		System.out.println("websocket " + Integer.toHexString(hashCode()) + ": onopen");
	}

	@Override
	protected void on_message(ByteBuffer bb) throws Throwable {
		System.out.println("websocket " + Integer.toHexString(hashCode()) + ": onmessage size:" + bb.remaining());
		send(new String(bb.array(),bb.position(),bb.remaining()));
		return;
//		if(bb.remaining()==0) {
//			send("");
//			return;
//		}
//		String msg = new String(bb.array(), bb.position(), bb.remaining());
//		Timestamp ts = new Timestamp(System.currentTimeMillis());
//		String s = ts.toString() + " " + req.get().ip() + " " + msg;
//		send(new ByteBuffer[] { ByteBuffer.wrap(send.getBytes()) }, true);
//		byte[]b=new byte[8*1024*1024];
//		for(int i=0;i<b.length;i++)
//			b[i]='0';
//		String s2=new String(b);
//		send(s2);
//		send(s);
	}

	synchronized final @Override protected void on_closed() throws Throwable {
		System.out.println("websocket " + Integer.toHexString(hashCode()) + ": onclosed");
	}
}
