// reviewed: 2024-08-05
package b;

import java.io.IOException;
import java.io.OutputStream;

/** Output stream that escapes the writes for a JavaScript string. */
public final class osjsstr extends OutputStream {
    private static final byte[] b_jsstr_sq = "\\'".getBytes();
    private static final byte[] b_jsstr_cr = "\\r".getBytes();
    private static final byte[] b_jsstr_nl = "\\n".getBytes();
    private static final byte[] b_jsstr_bs = "\\\\".getBytes();
    private static final byte[] b_jsstr_eof = "\\0".getBytes();
    private final OutputStream os;

    public osjsstr(final OutputStream os) {
        this.os = os;
    }

    @Override
    public void write(final int ch) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(final byte[] c) throws IOException {
        write(c, 0, c.length);
    }

    @Override
    public void write(final byte[] c, final int off, final int len) throws IOException {
        int from = 0;
        for (int i = 0; i < len; i++) {
            final byte b = c[off + i];
            if (b == '\n') {
                final int n = i - from;
                if (n != 0) {
                    os.write(c, off + from, n);
                }
                os.write(b_jsstr_nl);
                from = i + 1;
            } else if (b == '\r') {
                final int n = i - from;
                if (n != 0) {
                    os.write(c, off + from, n);
                }
                os.write(b_jsstr_cr);
                from = i + 1;
            } else if (b == '\'') {
                final int n = i - from;
                if (n != 0) {
                    os.write(c, off + from, n);
                }
                os.write(b_jsstr_sq);
                from = i + 1;
            } else if (b == '\\') {
                final int n = i - from;
                if (n != 0) {
                    os.write(c, off + from, n);
                }
                os.write(b_jsstr_bs);
                from = i + 1;
            } else if (b == '\0') {
                final int n = i - from;
                if (n != 0) {
                    os.write(c, off + from, n);
                }
                os.write(b_jsstr_eof);
                from = i + 1;
            }
        }
        final int n = len - from;
        if (n != 0) {
            os.write(c, off + from, n);
        }
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public String toString() {
        return os.toString();
    }

}
