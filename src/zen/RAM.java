package zen;

import b.a;
import b.xwriter;

public class RAM extends a {
	private static final long serialVersionUID = 1L;

	public short[] ram;

	@Override
	public void to(xwriter x) throws Throwable {
		x.p("RAM").br();
		int i = 0;
		for (int s = 0; s < 4; s++) {
			for (int r = 0; r < 4; r++) {
				for (int c = 0; c < 4; c++) {
					x.p(String.format("%04x", ram[i] & 0xffff)).p(' ');
					i++;
				}
				x.br();
			}
			x.br();
		}
	}

	public final void x_c(final xwriter x, final String param)
			throws Throwable {
		x.xalert("compile");
	}
}
