package zen;

import b.a;
import b.xwriter;

public class Registers extends a {
    private final static long serialVersionUID = 1;

    public short[] regs;

    @Override
    public void to(final xwriter x) throws Throwable {
        int i = 0;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                x.p(String.format("%04x", regs[i] & 0xffff)).p(' ');
                i++;
            }
            x.br();
        }
    }

}
