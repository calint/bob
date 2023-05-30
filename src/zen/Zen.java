package zen;

import b.a;
import b.xwriter;
import zen.emu.Core;
import zen.emu.SoC;

public class Zen extends a {
	private static final long serialVersionUID = 1L;

	public SoC soc;

	@Override
	public void to(xwriter x) throws Throwable {
		final Core c = soc.core();
		x.p("t=").p(c.getTck()).br();
		x.p("pc=").p(c.getPc()).br();
		x.p("zn=").p(c.isZf() ? 1 : 0).p(c.isNf() ? 1 : 0).br();
		x.p("leds=").p(c.getLeds()).br();
	}

	public final void x_c(final xwriter x, final String param)
			throws Throwable {
		x.xalert("compile");
	}
}
