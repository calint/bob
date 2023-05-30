package zen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
	public LineNums lineNums;

	public One() throws Throwable {
		ram.ram = soc.ram;
		core.core = soc.core;
		core.init();
		final String s = readResourceAsString("/zen/emu/tests/TB_Top1.zasm");
		src.set(s);
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
		// x.p(".row{display:flex;}").nl();
		// x.p(".col1,.col3{flex:1}").nl();
		// x.p(".col2{flex:2;}").nl();
		x.p(".row{text-align:center;white-space:nowrap;}");
		x.p(".col1,.col2,.col3,.col4{display:inline-block;width:fit-content;vertical-align:top;padding:1rem;}");
		x.p(".col3{width:3rem;height:" + LineNums.LINE_NUMS
				+ "rem;text-align:right;background:lightgray;padding-right:0.5rem;padding-left:0}");
		x.p(".col4{width:40rem;height:" + LineNums.LINE_NUMS + "rem;overflow-wrap:normal;white-space:pre;spell-check:false;}");
		x.style_();
		x.divo(this, "row", null).tagoe();
		x.divh(core, "col1");
		x.divh(ram, "col2");
		x.divh(lineNums, "col3");
		x.inptxtarea(src, "col4");
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

	public static String readResourceAsString(String resourceName) throws IOException {
		StringBuilder sb = new StringBuilder();

		InputStream is = One.class.getResourceAsStream(resourceName);
		InputStreamReader isr = new InputStreamReader(is, "utf8");
		BufferedReader br = new BufferedReader(isr);
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line).append("\n");
		}
		br.close();
		return sb.toString();
	}
}
