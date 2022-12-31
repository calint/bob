package bob.app;

import b.a;
import b.a_ajaxsts;
import b.xwriter;

public final class ControllerPostback extends a {
	private static final long serialVersionUID = 3L;

	public Ctrl c;
	public a_ajaxsts ajaxsts;

	@Override
	public void to(final xwriter x) throws Throwable {
		x.pl("<title>bob</title>");
		x.pl("<link rel=stylesheet href=/bob.css>");
		ajaxsts.to(x);
		x.nl();
		x.divh(c, "disp").nl();
	}
}
