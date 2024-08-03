package b;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/** Chunked output stream. */
final class oschunked extends OutputStream {
	private static final ByteBuffer bb_eochunk = ByteBuffer.wrap("0\r\n\r\n".getBytes());
	private static final ByteBuffer bb_crnl = ByteBuffer.wrap("\r\n".getBytes());
	private final req r;
	private final int chunk_size_bytes;
	private final byte[] chunkhx;
	private final byte[] buf;
	private int bufi;
	private final ByteBuffer[] headers_bb;
	private final int headers_bb_len;
	private boolean first_send = true;

	oschunked(final req r, final ByteBuffer[] headers_bb, final int headers_bb_len, final int chunk_size_bytes) {
		this.r = r;
		this.headers_bb = headers_bb;
		this.headers_bb_len = headers_bb_len;
		this.chunk_size_bytes = chunk_size_bytes;
		chunkhx = (Integer.toHexString(chunk_size_bytes) + "\r\n").getBytes();
		buf = new byte[chunk_size_bytes];
	}

	@Override
	public String toString() {
		return new String(buf, 0, bufi);
	}

	@Override
	public void write(final int ch) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(final byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(final byte[] c, int off, int len) throws IOException {
		final int remain = buf.length - bufi;
		if (len <= remain) {
			System.arraycopy(c, off, buf, bufi, len);
			bufi += len;
			return;
		}
		System.arraycopy(c, off, buf, bufi, remain);
		bufi += remain;
		off += remain;
		len -= remain;
		final ByteBuffer[] bba = { ByteBuffer.wrap(chunkhx), ByteBuffer.wrap(buf, 0, bufi), bb_crnl.slice() };
		write_blocking(bba);
		while (len > chunk_size_bytes) {
			final ByteBuffer[] bba2 = { ByteBuffer.wrap(chunkhx), ByteBuffer.wrap(c, off, chunk_size_bytes),
					bb_crnl.slice() };
			write_blocking(bba2);
			off += chunk_size_bytes;
			len -= chunk_size_bytes;
		}
		if (len > 0) {
			System.arraycopy(c, off, buf, 0, len);
			bufi = len;
		}
	}

	private void write_blocking(final ByteBuffer[] bba) throws IOException {
		final ByteBuffer[] send_buffers;
		if (first_send) { // include the header buffers at first send
			first_send = false;
			send_buffers = new ByteBuffer[headers_bb_len + bba.length];
			for (int i = 0; i < headers_bb_len; i++) {
				send_buffers[i] = headers_bb[i];
			}
			final int n = headers_bb_len + bba.length;
			for (int i = headers_bb_len, j = 0; i < n; i++, j++) {
				send_buffers[i] = bba[j];
			}
		} else {
			send_buffers = bba;
		}
		long remaining = 0;
		for (final ByteBuffer bb : send_buffers) {
			remaining += bb.remaining();
		}
		while (remaining != 0) {
			final long c = r.socket_channel.write(send_buffers, 0, send_buffers.length);
			if (c == 0) {
				// System.out.println("oschunked blocked rem:"+remaining);
				synchronized (r) {
					r.oschunked_waiting_write(true);
					r.selection_key.interestOps(SelectionKey.OP_WRITE);
					r.selection_key.selector().wakeup();
					try {
						r.wait();
					} catch (final InterruptedException ok) {
					}
					r.oschunked_waiting_write(false);
				}
			}
			remaining -= c;
			thdwatch.output += c;
		}
	}

	@Override
	public void flush() throws IOException {
		if (bufi == 0)
			return;
		final ByteBuffer[] bba = { ByteBuffer.wrap(Integer.toHexString(bufi).getBytes()), bb_crnl.slice(),
				ByteBuffer.wrap(buf, 0, bufi), bb_crnl.slice() };
		write_blocking(bba);
		bufi = 0;
	}

	void finish() throws IOException {
		flush();
		write_blocking(new ByteBuffer[] { bb_eochunk.slice() });
	}
}
