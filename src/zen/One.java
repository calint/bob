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
	public CoreDisp core;

	public One() {
		ram.ram = soc.ram;
		core.core = soc.core;
		core.init();
	}

	@Override
	public void to(xwriter x) throws Throwable {
		x.p("zen-one emulator");
		x.br().br();
		x.ax(this, "s", "save");
		x.p(" ");
		x.ax(this, "c", "compile");
		x.p(" ");
		x.ax(this, "t", "step");
		x.p(" ");
		x.ax(this, "r", "run");
		x.p(" ");
		x.ax(this, "rst", "reset");
		x.br().br();
		x.style();
//		x.p(".row{display:flex;}").nl();
//		x.p(".col1,.col3{flex:1}").nl();
//		x.p(".col2{flex:2;}").nl();
		x.p(".row{text-align:center;}");
		x.p(".col1,.col2,.col3{display:inline-block;width: fit-content;vertical-align:top;padding:1rem}");
		x.p(".col3{width:30rem;height:256rem;padding:1rem}");
		x.style_();
		x.divo(this, "row", null).tagoe();
		x.divh(core, "col1");
		x.divh(ram, "col2");
		x.inptxtarea(src, "col3");
		x.div_();
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
