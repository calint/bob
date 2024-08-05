// reviewed: 2024-08-05
package b;

import b.a.stateless;

/** Endpoint displaying server stats. */
public @stateless class a_stats extends a implements bin {
	private static final long serialVersionUID = 1;

	public String content_type() {
		return "text/plain;charset=utf-8";
	}

	@Override
	public void to(final xwriter x) throws Throwable {
		thdwatch.update();
		thdwatch.print_fields3_to(x.outputstream());
	}

}
