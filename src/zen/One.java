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
	public Zen zen;
	
	public One() {
		ram.ram = soc.getRAM();
		zen.soc = soc;
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
		x.br();
		x.inptxtarea(src, "large");
		x.divh(ram);
		x.divh(zen);
	}
	
	public final void x_c(final xwriter x, final String param) throws Throwable {
		Zasm.compile(src.toString(), soc.getRAM());
		soc.reset();
		x.xu(ram);
		x.xu(zen);
	}	

	public final void x_t(final xwriter x, final String param) throws Throwable {
		soc.tick();
		x.xu(zen);
	}	
}
