// reviewed: 2024-08-05
package b;

import b.b.conf;

/** AJAX status field used by front-end. */
public class a_ajaxsts extends a {
	private static final long serialVersionUID = 1;

	@Override
	public void to(final xwriter x) throws Throwable {
		x.divo(this, null, sts_css_opened).tagoe().div_();
	}

	@conf
	public static String sts_css_opened = "text-align:center;margin:auto;transition-duration:1s;transition-timing-function:ease;padding:.5em;box-shadow:0 0 .5em rgba(0,0,0,.5);";
	@conf
	public static String sts_css_closed = "text-align:center;margin:auto;transition-duration:1s;transition-timing-function:ease;padding:.5em;box-shadow:0 0 0 rgba(0,0,0,0);";
}
