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
	private Zasm zasm;

	public RAM r;
	public CoreUI c;
	public LineNums l;
	public a s;
	public Terminal t;

	public One() throws Throwable {
		r.ram = soc.ram;
		c.core = soc.core;
		c.init();
		final String src = readResourceAsString("/zen/emu/tests/input-print.zasm");
		s.set(src);
	}

	@Override
	public void to(xwriter x) throws Throwable {
		x.tago("div").attr("class", "topmenu").tagoe();
		// x.ax(this, "s", "save");
		// x.p(" ");
		x.ax(this, "c", "compile");
		x.p(" ");
		x.a("javascript:zen_run_start()", "run");
		x.p(" ");
		x.a("javascript:zen_run_stop()", "stop");
		x.p(" ");
		x.a("javascript:zen_tick()", "step");
		x.p(" ");
		x.a("javascript:zen_reset()", "reset");
		x.p(" ");
		x.div_();

		x.br();
		x.p("zen-one emulator");
		x.br().br();

		x.divh(t, "term");

		x.p("type here: ").tago("input").attr("class", "nbr")
				.attr("onkeydown", "this.value='';zen_tx_buf.push(event.keyCode)")
				.tagoe();

		x.br().br();
		x.tago("div").attr("class", "row").tagoe();
		x.divo(this, "row", null).tagoe();
		x.divh(c, "col1");
		x.divh(r, "col2");
		x.divh(l, "col3");
		x.inptxtarea(s, "col4");
		x.div_();
	}

	public final void x_c(final xwriter x, final String param) throws Throwable {
		soc.reset();
		zasm = new Zasm();
		zasm.compile(s.toString(), soc.ram);
		x.xu(r);
		x.xu(c);
		// selectSourceRange(x);
	}

	private boolean selectActiveInstruction = true;

	public final void x_t(final xwriter x, final String param) throws Throwable {
		final int key = Integer.parseInt(param);
		if (key != 0) {
			soc.urx.data = key;
			soc.urx.dr = true;
			System.out.println(key);
		}
		soc.tick();
		if (soc.utx.go) {
			x.xp(t, String.valueOf((char) soc.utx.dataImm8));
			soc.utx.go = false;
		}
		x.xu(r);
		x.xu(c);
		// if (selectActiveInstruction) {
		// 	selectSourceRange(x);
		// }
	}

	public final void x_rst(final xwriter x, final String param) throws Throwable {
		soc.reset();
		x.xu(r);
		x.xu(c);
		x.xu(t);
		// if (selectActiveInstruction) {
		// 	selectSourceRange(x);
		// }
	}

	public final void x_r(final xwriter x, final String param) throws Throwable {
		selectActiveInstruction = !selectActiveInstruction;
	}

	// private void selectSourceRange(final xwriter x) throws Throwable {
	// 	if (zasm == null)
	// 		return;
	// 	final int[] rng = zasm.getInstructionSourceRange(soc.core.pc);
	// 	final String sid = s.id();
	// 	x.xfocus(sid).p("$('" + sid + "').setSelectionRange(" + rng[0] + "," + rng[1] + ");");
	// }

	// public final void x_key(final xwriter x, final String param) throws Throwable
	// {
	// System.out.println(param);
	// soc.urx.data = Integer.parseInt(param);
	// soc.urx.dr = true;
	// }

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
