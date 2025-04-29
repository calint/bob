//
// reviewed: 2025-04-29
//
package zen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import b.a;
import b.xwriter;
import zen.emu.SoC;
import zen.zasm.Zasm;

public class UI extends a {
    private final static long serialVersionUID = 1L;

    private final SoC soc = new SoC();
    private Zasm zasm;

    public RAM r;
    public Panel p;
    public a t; // terminal
    public LineNums l;
    public a s; // source editor

    public UI() throws Throwable {
        r.ram = soc.ram;
        p.c = soc.core;
        p.init();
        final String src = readResourceAsString("/zen/emu/tests/input-print.zasm");
        s.set(src);
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        x.tago("div").attr("class", "topmenu").tagoe();
        x.ax(this, "c", "compile");
        x.p(" ");
        x.a("javascript:zen_run_start()", "run");
        x.p(" ");
        x.a("javascript:zen_run_stop()", "stop");
        x.p(" ");
        x.a("javascript:zen_tick()", "step");
        x.p(" ");
        x.a("javascript:zen_reset()", "reset");
        x.div_();

        x.br().br().br();
        x.p("zen-one emulator");
        x.br().br();

        x.divh(t, "term");

        x.p("type here -&gt; ").tago("input").attr("class", "nbr")
                .attr("onkeydown", "this.value='';zen_tx_buf.push(event.keyCode)").tagoe();

        x.br().br();
        x.tago("div").attr("class", "row").tagoe();
        x.divo(this, "row", null).tagoe();
        x.divh(p, "col1");
        x.divh(r, "col2");
        x.divh(l, "col3");
        x.inptxtarea(s, "col4");
        x.script().p("$('").p(s.id()).p("').spellcheck=false;").script_();
        x.div_();
    }

    /** compile */
    public final void x_c(final xwriter x, final String param) throws Throwable {
        soc.reset();
        zasm = new Zasm();
        zasm.compile(s.toString(), soc.ram);
        x.xu(r);
        x.xu(p);
        x.xu(t);
    }

    /** tick */
    public final void x_t(final xwriter x, final String param) throws Throwable {
        final int key = Integer.parseInt(param);
        if (key != 0) {
            soc.urx.data = key;
            soc.urx.dr = true;
        }
        soc.tick();
        if (soc.utx.go) {
            x.xp(t, String.valueOf((char) soc.utx.data));
            soc.utx.go = false;
        }
        x.xu(r);
        x.xu(p);
        selectSourceRange(x);
    }

    /** fast tick used by zen.js */
    public final void x_ft(final xwriter x, final String param) throws Throwable {
        final int key = Integer.parseInt(param);
        if (key != 0) {
            soc.urx.data = key;
            soc.urx.dr = true;
        }
        soc.tick();
        if (soc.utx.go) {
            x.xp(t, String.valueOf((char) soc.utx.data));
            soc.utx.go = false;
        }
        x.xu(r);
        x.xu(p);
    }

    /** reset */
    public final void x_rst(final xwriter x, final String param) throws Throwable {
        soc.reset();
        x.xu(r);
        x.xu(p);
        x.xu(t);
    }

    private void selectSourceRange(final xwriter x) throws Throwable {
        if (zasm == null) {
            return;
        }
        final int[] rng = zasm.getInstructionSourceRange(soc.core.pc);
        final String sid = s.id();
        x.xfocus(sid).p("$('" + sid + "').setSelectionRange(" + rng[0] + "," + rng[1] + ");");
    }

    public static String readResourceAsString(final String resourceName) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final InputStream is = UI.class.getResourceAsStream(resourceName);
        final InputStreamReader isr = new InputStreamReader(is, "utf8");
        final BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }

}
