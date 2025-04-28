package zen;

import b.a;
import b.xwriter;
import zen.emu.Core;

public class Panel extends a {
    private final static long serialVersionUID = 1;

    public Core core;
    public Registers regs;

    public void init() {
        regs.regs = core.regs;
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        x.p("t:").p(core.tick).br();
        x.p(core.zf ? "z" : "-").p(core.nf ? "n" : "-").p(" ");
        int leds = core.leds;
        for (int i = 0; i < 4; i++) {
            x.p((leds & 8) != 0 ? "o" : ".");
            leds <<= 1;
        }
        x.br();
        x.p("[").p(core.pc).p("]:").p(String.format("%04X", core.ram[core.pc])).br().br();
        regs.to(x);
        x.br();
        final int n = core.cs.mem.length > 32 ? 32 : core.cs.mem.length;
        for (int i = 0; i < n; i++) {
            final int pc = core.cs.mem[i] & 0xffff;
            final boolean zf = (core.cs.mem[i] & 0x10000) != 0;
            final boolean nf = (core.cs.mem[i] & 0x20000) != 0;
            x.p(core.cs.idx == i ? ">" : " ").p(zf ? "z" : "-").p(nf ? "n" : "-").p(" ").p(String.format("%04x", pc))
                    .p(" ");
            if (i % 2 == 1) {
                x.br();
            }
        }
    }

}
