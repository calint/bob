//
// reviewed: 2025-04-29
//
package zen;

import b.a;
import b.xwriter;
import zen.emu.Core;

public class Panel extends a {

    private final static long serialVersionUID = 1;

    public Core c;
    public Registers r;

    public void init() {
        r.regs = c.regs;
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        x.p("t:").p(c.tick).br();
        x.p(c.zf ? "z" : "-").p(c.nf ? "n" : "-").p(" ");
        int leds = c.leds;
        for (int i = 0; i < 4; i++) {
            x.p((leds & 8) != 0 ? "o" : ".");
            leds <<= 1;
        }
        x.br();
        x.p("[").p(c.pc).p("]:").p(String.format("%04X", c.ram[c.pc])).br().br();
        r.to(x);
        x.br();
        final int n = c.cs.mem.length > 32 ? 32 : c.cs.mem.length;
        for (int i = 0; i < n; i++) {
            final int pc = c.cs.mem[i] & 0xffff;
            final boolean zf = (c.cs.mem[i] & 0x10000) != 0;
            final boolean nf = (c.cs.mem[i] & 0x20000) != 0;
            x.p(c.cs.idx == i ? ">" : " ").p(zf ? "z" : "-").p(nf ? "n" : "-").p(" ").p(String.format("%04x", pc))
                    .p(" ");
            if (i % 2 == 1) {
                x.br();
            }
        }
    }

}
