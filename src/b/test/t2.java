package b.test;

import b.a;
import b.xwriter;

/** large chunked write that will require multiple writes. */
public class t2 extends a {
    private static final long serialVersionUID = 1;

    @Override
    public void to(final xwriter x) throws Throwable {
        final byte[] ba = new byte[32 * 1024 * 1024];
        byte ch = (byte) 'a';
        for (int i = 0; i < ba.length; i++) {
            ba[i] = ch;
            ch++;
            if (ch > (byte) 'z') {
                ch = 'a';
            }
        }
        x.outputstream().write(ba);
        // x.p(new String(ba,0,ba.length));
    }
}
