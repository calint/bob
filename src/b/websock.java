// reviewed: 2024-08-05
// reviewed: 2024-08-12
package b;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;

/**
 * Websocket implementation of half duplex mode. Use case is request response
 * chain with one send for each on_message.
 */
public abstract class websock {
    private enum state {
        handshake, parse_next_frame, parse_data, send, closed
    }

    private req rq;
    private SocketChannel socket_channel;
    private ByteBuffer bb;
    private state st = state.handshake;
    private int payload_remaining;
    private ByteBuffer bb_message;
    private ByteBuffer[] send_bba;
    private boolean is_first_packet;
    private final byte[] mask_key = new byte[4];
    private final boolean is_threaded;
    private int mask_i;
    boolean is_masked;

    /**
     * @param is_threaded true to handle on_message on a thread. If websock is not
     *                    threaded it will run on the server thread potentially
     *                    blocking.
     */
    public websock(final boolean is_threaded) {
        this.is_threaded = is_threaded;
    }

    /** @param r the request that runs this websocket. */
    final synchronized void init(final req r) throws Throwable {
        rq = r;
        socket_channel = r.socket_channel;
        bb = r.bb;
        // rfc6455#section-1.3
        // Opening Handshake
        final String key = r.headers().get("sec-websocket-key");
        final String s = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        final byte[] sha1ed = MessageDigest.getInstance("SHA-1").digest(s.getBytes());
        final String replkey = base64.encodeToString(sha1ed, true);
        final ByteBuffer bbo = ByteBuffer.allocate(b.K);
        // final String prot=hdrs.get("sec-webSocket-protocol");
        bbo.put("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: "
                .getBytes());
        bbo.put(replkey.getBytes());
        // bbo.put("\r\nSec-WebSocket-Protocol: chat".getBytes());
        if (r.session_id_set) {
            bbo.put(req.hk_set_cookie);
            bbo.put(r.session_id().getBytes());
            bbo.put(req.hkv_set_cookie_append);
            r.session_id_set = false;
        }
        bbo.put("\r\n\r\n".getBytes());
        bbo.flip();
        while (bbo.hasRemaining() && socket_channel.write(bbo) != 0) {
        }
        if (bbo.hasRemaining()) {
            throw new RuntimeException("initiation packet not fully sent");
        }
        on_opened();
        assert (bb.remaining() == 0);
        if (is_sending()) {
            // note: when 'process()' is called and bb.remaining()==0 then the correct state
            // will be set
            return;
        }
        st = state.parse_next_frame;
        // response sent, wait for packet (assumes client hasn't sent anything yet)
        r.selection_key.interestOps(SelectionKey.OP_READ);
        if (is_threaded) {
            r.selection_key.selector().wakeup();
        }
    }

    private boolean is_sending() {
        return st == state.send;
    }

    /** Called by b whenever there is read or write to socket available. */
    final synchronized void process() throws Throwable {
        while (true) {
            if (is_sending()) {
                write();
                if (is_sending()) {
                    // not done sending
                    return;
                }
            }
            if (bb.remaining() == 0) {
                // done parsing incoming buffer
                bb.clear();
                final int n = socket_channel.read(bb);
                bb.flip();
                if (n == 0) {
                    rq.selection_key.interestOps(SelectionKey.OP_READ);
                    rq.selection_key.selector().wakeup();
                    return;
                }
                if (n == -1) {
                    st = state.closed;
                    rq.close();
                    thdwatch.socks--;
                    return; // todo: on_connection_lost called when request is closed
                }
                thdwatch.input += n;
            }
            // parse incoming buffer
            while (bb.remaining() != 0) {
                switch (st) {
                case parse_next_frame:
                    // note: assuming the complete header is buffered. breaking up into states for
                    // header would handle the input buffer of 1 B

                    // rfc6455#section-5.2
                    // Base Framing Protocol
                    final int b0 = bb.get();
                    // final boolean fin = (b0 & 128) == 128;
                    // final int resv = (b0 >> 4) & 7;
                    // if (resv != 0) {
                    // throw new Error("reserved bits are not 0");
                    // }
                    final int opcode = b0 & 0xf;
                    if (opcode == 8) {
                        // rfc6455#section-5.5.1
                        st = state.closed;
                        rq.close();
                        thdwatch.socks--;
                        return;
                    }
                    // handle the other opcodes
                    // https://www.rfc-editor.org/rfc/rfc6455#section-5.2
                    // ping/pong not implemented in FireFox and Chromium as of 2022-12-26

                    // parse header
                    final int b1 = bb.get();
                    is_masked = (b1 & 128) == 128;
                    payload_remaining = b1 & 127;
                    if (payload_remaining == 126) {
                        final int by2 = (bb.get() & 0xff) << 8;
                        final int by1 = bb.get() & 0xff;
                        payload_remaining = by2 | by1;
                    } else if (payload_remaining == 127) {
                        bb.get(); // skip the bytes that encode a length > 4 GB
                        bb.get();
                        bb.get();
                        bb.get();
                        final int by4 = (bb.get() & 0xff) << 24;
                        final int by3 = (bb.get() & 0xff) << 16;
                        final int by2 = (bb.get() & 0xff) << 8;
                        final int by1 = bb.get() & 0xff;
                        payload_remaining = by4 | by3 | by2 | by1;
                    }
                    bb.get(mask_key);
                    is_first_packet = true;
                    mask_i = 0;
                    st = state.parse_data;
                    // fall through
                case parse_data:
                    final byte[] bba = bb.array();
                    final int pos = bb.position();
                    final int limit = bb.remaining() > payload_remaining ? pos + payload_remaining : bb.limit();
                    // if (is_masked && mask_key[0] == 0 && mask_key[1] == 0 && mask_key[2] == 0 &&
                    // mask_key[3] == 0) {
                    // throw new RuntimeException();
                    // }
                    // unmask
                    for (int i = pos; i < limit; i++) {
                        final byte b = (byte) (bba[i] ^ mask_key[mask_i]);
                        bba[i] = b;
                        mask_i++;
                        if (mask_i == mask_key.length) {
                            mask_i = 0;
                        }
                    }
                    bb.position(limit); // sets to the current position
                    final int read_length = limit - pos; // number of bytes read from the buffer
                    payload_remaining -= read_length;
                    if (payload_remaining == 0) {
                        // data has been fully read
                        st = state.parse_next_frame;
                    }
                    final ByteBuffer bb_payload = ByteBuffer.wrap(bba, pos, read_length);
                    // note: position is at start of data, limit is at the end of unmasked data
                    on_payload(bb_payload);
                    is_first_packet = false;
                    if (is_sending()) {
                        // note: maybe on_payload -> on_message triggered sending
                        return;
                    }
                    break;
                default:
                    throw new RuntimeException();
                }
            }
        }
    }

    private void on_payload(final ByteBuffer bb) throws Throwable {
        final boolean is_last_packet = payload_remaining == 0;
        if (!is_last_packet) {
            if (is_first_packet) {
                bb_message = ByteBuffer.allocate(bb.remaining() + payload_remaining);
                bb_message.put(bb);
                return;
            } else {
                bb_message.put(bb);
            }
            return;
        }
        // last packet
        if (!is_first_packet) {
            bb_message.put(bb);
            bb_message.flip();
        } else {
            bb_message = bb;
        }
        on_message(bb_message);
        bb_message = null;
    }

    private void write() throws Throwable {
        final long n = socket_channel.write(send_bba);
        thdwatch.output += n;
        // check if the write is complete.
        for (final ByteBuffer b : send_bba) {
            if (b.hasRemaining()) {
                // buffer not fully sent, request socket write
                rq.selection_key.interestOps(SelectionKey.OP_WRITE);
                if (is_threaded) {
                    rq.selection_key.selector().wakeup();
                }
                return;
            }
        }
        send_bba = null;
        st = state.parse_next_frame;
    }

    abstract protected void on_opened() throws Throwable;

    /** Called when the web socket has been closed. */
    abstract protected void on_closed() throws Throwable;

    /**
     * Called when a message has been decoded. ByteBuffer position is at start of
     * data and limit marks the end of data.
     */
    abstract protected void on_message(ByteBuffer bb) throws Throwable;

    protected final void send(final String s) throws Throwable {
        send(new ByteBuffer[] { ByteBuffer.wrap(s.getBytes()) }, true);
    }

    protected final void send(final ByteBuffer bb, final boolean textmode) throws Throwable {
        send(new ByteBuffer[] { bb }, textmode);
    }

    protected final void send(final ByteBuffer[] bba, final boolean textmode) throws Throwable {
        if (is_sending()) {
            throw new RuntimeException("Trying to send while busy sending. Only one send per on_message.");
            // note. before the request is closed by the exception handler there might be
            // attempted reads which throw closed channel exception.
        }
        int nbytes_to_send = 0;
        for (final ByteBuffer b : bba) {
            nbytes_to_send += b.remaining();
        }
        send_bba = new ByteBuffer[bba.length + 1];
        send_bba[0] = make_header(nbytes_to_send, textmode);
        for (int i = 1; i < send_bba.length; i++) {
            send_bba[i] = bba[i - 1];
        }
        st = state.send;
        write();
    }

    private static ByteBuffer make_header(final int size_of_data_to_send, final boolean text_mode) {
        // rfc6455#section-5.2
        // Base Framing Protocol
        int nhdr;
        final byte[] hdr = new byte[10];
        hdr[0] = (byte) ((text_mode ? 1 : 2) | 128);
        if (size_of_data_to_send <= 125) {
            hdr[1] = (byte) size_of_data_to_send;
            nhdr = 2;
        } else if (size_of_data_to_send <= 65535) {
            hdr[1] = 126;
            hdr[2] = (byte) (size_of_data_to_send >> 8 & 255);
            hdr[3] = (byte) (size_of_data_to_send & 255);
            nhdr = 4;
        } else {
            hdr[1] = 127;
            // hdr[2]=(byte)((ndata>>56)&255); // ignore size bigger than 4 GB
            // hdr[3]=(byte)((ndata>>48)&255);
            // hdr[4]=(byte)((ndata>>40)&255);
            // hdr[5]=(byte)((ndata>>32)&255);
            hdr[6] = (byte) (size_of_data_to_send >> 24 & 255);
            hdr[7] = (byte) (size_of_data_to_send >> 16 & 255);
            hdr[8] = (byte) (size_of_data_to_send >> 8 & 255);
            hdr[9] = (byte) (size_of_data_to_send & 255);
            nhdr = 10;
        }
        return ByteBuffer.wrap(hdr, 0, nhdr);
    }

    protected final req req() {
        return rq;
    }

    final boolean is_threaded() {
        return is_threaded;
    }

    @Override
    public String toString() {
        return new String(bb.array(), bb.position(), bb.remaining());
    }
}
