package b.test;

import b.a;
import b.xwriter;

/** hello world */
public class t4 extends a {
	static final long serialVersionUID = 3;

	@Override
	public void to(final xwriter x) throws Throwable {
		x.title(getClass().getName());
		x.pl("hello world statefull");
	}
}
