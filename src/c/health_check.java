package c;

import b.a;
import b.xwriter;

public class health_check extends a {
	private static final long serialVersionUID = 1L;

	@Override
	public void to(xwriter x) throws Throwable {
		x.pl("ok");
	}
}
