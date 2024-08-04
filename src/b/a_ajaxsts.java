package b;

import b.b.conf;

/** AJAX status field used by front-end. */
public class a_ajaxsts extends a {
	@Override
	public void to(final xwriter x) throws Throwable {
		// ? review this
		x.p("<div id=").p(id()).p(" style=\"").p(sts_css_opened).p("\"></div>");
	}

	@conf
	public static String sts_css_opened = "text-align:center;margin:auto;transition-duration:1s;transition-timing-function:ease;padding:.5em;box-shadow:0 0 .5em rgba(0,0,0,.5);";
	@conf
	public static String sts_css_closed = "text-align:center;margin:auto;transition-duration:1s;transition-timing-function:ease;padding:.5em;box-shadow:0 0 0 rgba(0,0,0,0);";

	private static final long serialVersionUID = 1;
}
