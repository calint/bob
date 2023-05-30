package zen;

import b.a;
import b.xwriter;
import zen.emu.SoC;
import zen.zasm.Zasm;

public class One extends a {
	private static final long serialVersionUID = 1L;

	final private SoC soc = new SoC();

	public RAM ram;
	public a src;
	public Core core;

	public One() {
		ram.ram = soc.ram;
		core.core = soc.core;
		core.init();
	}

	@Override
	public void to(xwriter x) throws Throwable {
		x.p("zen-one emulator and integrated development environment");
		x.br();
		x.ax(this, "s", "save");
		x.p(" ");
		x.ax(this, "c", "compile");
		x.p(" ");
		x.ax(this, "t", "step");
		x.p(" ");
		x.ax(this, "r", "run");
		x.p(" ");
		x.ax(this, "rst", "reset");
		x.br();
		x.inptxtarea(src, "large");
		x.divh(core);
		x.divh(ram);
	}

	public final void x_c(final xwriter x, final String param) throws Throwable {
		Zasm.compile(src.toString(), soc.ram);
		soc.reset();
		x.xu(ram);
		x.xu(core);
	}

	public final void x_t(final xwriter x, final String param) throws Throwable {
		soc.tick();
		x.xu(ram);
		x.xu(core);
	}

	public final void x_rst(final xwriter x, final String param) throws Throwable {
		soc.reset();
		x.xu(ram);
		x.xu(core);
	}

}
