// reviewed: 2024-08-05
package b;

import java.io.IOException;
import java.io.OutputStream;

/** Output stream with callbacks at newline. */
public class osnl extends OutputStream {
    private final StringBuilder line = new StringBuilder(256);

    @Override
    public final void write(final int c) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void write(final byte[] c) throws IOException {
        write(c, 0, c.length);
    }

    @Override
    public final void write(final byte[] c, final int off, final int len) throws IOException {
        for (int i = 0; i < len; i++) {
            final byte b = c[off + i];
            if (b == '\n') {
                try {
                    on_newline(line.toString());
                } catch (final Throwable e) {
                    throw new Error(e);
                }
                line.setLength(0);
            } else {
                line.append((char) b);
            }
        }
    }

    public void on_newline(final String line) throws Throwable {
        System.out.println(line);
    }
}
