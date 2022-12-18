package bob;

import b.a;
import b.a_ajaxsts;
import b.xwriter;
import bob.elem.ctrl;

public class controller_postback extends a{
	private static final long serialVersionUID=2L;

	public ctrl c;
	public a_ajaxsts ajaxsts;

	@Override public void to(xwriter x) throws Throwable{
		x.pl("<title>bob</title>");
		x.pl("<link rel=stylesheet href=/bob.css>");
		ajaxsts.to(x);
		x.divh(c,"disp");
		x.nl().nl();
	}
}
