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

	public RAM r;
	public CoreUI c;
	public LineNums l;
	public a s;
	public Terminal t;

	public One() throws Throwable {
		r.ram = soc.ram;
		c.core = soc.core;
		c.init();
		final String src = readResourceAsString("/zen/emu/tests/TB_Top1.zasm");
		s.set(src);
	}

	@Override
	public void to(xwriter x) throws Throwable {
		x.br();
		x.p("zen-one emulator");
		x.br().br();

		x.divh(t,"term");
		
		x.tago("input").attr("class", "inp")
				.attr("onkeydown", "this.value='';$x('" + id() + " key '+event.keyCode)")
				.tagoe();

		x.br().br();
		x.tago("div").attr("class","row").tagoe();
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
		
		x.divo(this, "row", null).tagoe();
		x.divh(c, "col1");
		x.divh(r, "col2");
		x.divh(l, "col3");
		x.inptxtarea(s, "col4");
		x.div_();
	}

	public final void x_c(final xwriter x, final String param) throws Throwable {
		Zasm.compile(s.toString(), soc.ram);
		soc.reset();
		x.xu(r);
		x.xu(c);
	}

	public final void x_t(final xwriter x, final String param) throws Throwable {
		soc.tick();
		x.xu(r);
		x.xu(c);
	}

	public final void x_rst(final xwriter x, final String param) throws Throwable {
		soc.reset();
		x.xu(r);
		x.xu(c);
	}

	public final void x_key(final xwriter x, final String param) throws Throwable {
		System.out.println(param);
		soc.urx.data = Integer.parseInt(param);
		soc.urx.dr = true;
		t.onKey(x, soc.urx.data);
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
