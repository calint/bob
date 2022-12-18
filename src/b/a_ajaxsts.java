package b;
import b.b.conf;
public class a_ajaxsts extends a{
	@Override public void to(final xwriter x) throws Throwable{
		// ? review this
//		x.p("<div onclick=\"console.log(event);let e=$('"+id()+"');console.log(e);e.style.cssText='"+sts_css_closed+"';\">");
		x.p("<div id=").p(id()).p(" style=\"").p(sts_css_opened).p("\"></div>");
//		x.div_();

	}

//	@conf public static String sts_css_opened="transition-duration:1s;transition-timing-function:ease;padding:1em;width:20em;height:1em;background:#fed;box-shadow:0 0 .5em rgba(0,0,0,.5);";
//	@conf public static String sts_css_closed="transition-duration:1s;transition-timing-function:ease;padding:1em;width:20em;height:1em;color:rgba(255,255,255,.5);box-shadow:0 0 0 rgba(0,0,0,0);";
//	@conf public static String sts_css_opened="position:fixed;right:0;bottom:0;transition-duration:1s;transition-timing-function:ease;padding:1em;background:#fed;box-shadow:0 0 .5em rgba(0,0,0,.5);";
//	@conf public static String sts_css_closed="position:fixed;right:0;bottom:0;transition-duration:1s;transition-timing-function:ease;padding:1em;color:rgba(255,255,255,.5);box-shadow:0 0 0 rgba(0,0,0,0);";
//	@conf public static String sts_css_opened="text-align:center;margin:auto;transition-duration:1s;transition-timing-function:ease;padding:.5em;background:#fed;box-shadow:0 0 .5em rgba(0,0,0,.5);";
//	@conf public static String sts_css_closed="text-align:center;margin:auto;transition-duration:1s;transition-timing-function:ease;padding:.5em;color:rgba(255,255,255,.5);box-shadow:0 0 0 rgba(0,0,0,0);";
	@conf public static String sts_css_opened="text-align:center;margin:auto;transition-duration:1s;transition-timing-function:ease;padding:.5em;box-shadow:0 0 .5em rgba(0,0,0,.5);";
	@conf public static String sts_css_closed="text-align:center;margin:auto;transition-duration:1s;transition-timing-function:ease;padding:.5em;box-shadow:0 0 0 rgba(0,0,0,0);";

	private static final long serialVersionUID=1;
}
