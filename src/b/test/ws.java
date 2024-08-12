package b.test;

import java.nio.ByteBuffer;

import b.websock;

/** Websocket that implements the framework. */
public class ws extends websock {
    public ws() {
        super(true);
    }

    @Override
    protected final void on_opened() throws Throwable {
    }

    @Override
    protected final void on_message(final ByteBuffer bb) throws Throwable {
        send(new String(bb.array(), bb.position(), bb.remaining()));
    }

    @Override
    protected final void on_closed() throws Throwable {
    }
}
