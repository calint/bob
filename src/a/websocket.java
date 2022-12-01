package a;

import java.nio.ByteBuffer;
import java.sql.Timestamp;

import b.req;
import b.threadedsock;
import b.websock;

final public class websocket extends websock implements threadedsock{

	synchronized final @Override protected void onopened() throws Throwable {
		System.out.println("onopened");
	}

	synchronized final @Override protected void onclosed() throws Throwable {
		System.out.println("onclosed");
	}

	@Override
	synchronized protected void onmessage(final ByteBuffer bb) throws Throwable {
		System.out.println("onmessage");
		String msg=new String(bb.array(),bb.position(),bb.remaining());
		System.out.println(msg);
		Timestamp ts=new Timestamp(System.currentTimeMillis());
		String send=ts.toString()+" "+req.get().ip()+" "+msg;
		send(new ByteBuffer[]{ByteBuffer.wrap(send.getBytes())},true);
	}
}
