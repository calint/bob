package zen;

import b.a;
import b.xwriter;
import zen.emu.Core;

public class CoreDisp extends a {
	private static final long serialVersionUID = 1L;

	public Core core;
	public Registers regs;

	public void init() {
		regs.regs = core.regs;
	}

	@Override
	public void to(xwriter x) throws Throwable {
		x.p("t:").p(core.tick).br();
		x.p(core.pc).p(" ").p(core.zf ? "z" : "-").p(core.nf ? "n" : "-").p(" ");
		int leds = core.leds;
		for (int i = 0; i < 4; i++) {
			x.p((leds & 8) != 0 ? "o" : ".");
			leds <<= 1;
		}
		x.p(" ").br().p(String.format("%04x", core.ram[core.pc])).br();
		//x.p("registers:").br();
		regs.to(x);
		//x.p("calls: ").p(core.cs.idx).br();
		final int n = core.cs.mem.length > 32 ? 32 : core.cs.mem.length;
		for (int i = 0; i < n; i++) {
			final int pc = core.cs.mem[i] & 0xffff;
			final boolean zf = (core.cs.mem[i] & 0x10000) != 0;
			final boolean nf = (core.cs.mem[i] & 0x20000) != 0;
			x.p(core.cs.idx == i ? ">" : " ").p(zf ? "z" : "-").p(nf ? "n" : "-").p(" ")
					.p(String.format("%04x", pc)).p(" ");
			if (i % 2 == 1) {
				x.br();
			}
		}
	}

	public final void x_c(final xwriter x, final String param)
			throws Throwable {
		x.xalert("compile");
	}
}
