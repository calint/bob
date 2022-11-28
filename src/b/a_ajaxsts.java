package b;
import b.b.conf;
public class a_ajaxsts extends a{
	@Override public void to(final xwriter x)throws Throwable{
		x.p("<span onclick=\"console.log(event);var e=$('"+id()+"');console.log(e);e.style.cssText='"+sts_css_closed+"';\">");
		x.span(this,sts_css_opened);
		x.span_();
	}
	
//	@conf public static String sts_css_opened="transition-duration:1s;transition-timing-function:ease;padding:1em;width:20em;height:1em;background:#fed;box-shadow:0 0 .5em rgba(0,0,0,.5);";
//	@conf public static String sts_css_closed="transition-duration:1s;transition-timing-function:ease;padding:1em;width:20em;height:1em;color:rgba(255,255,255,.5);box-shadow:0 0 0 rgba(0,0,0,0);";
//	@conf public static String sts_css_opened="position:fixed;right:0;bottom:0;transition-duration:1s;transition-timing-function:ease;padding:1em;background:#fed;box-shadow:0 0 .5em rgba(0,0,0,.5);";
//	@conf public static String sts_css_closed="position:fixed;right:0;bottom:0;transition-duration:1s;transition-timing-function:ease;padding:1em;color:rgba(255,255,255,.5);box-shadow:0 0 0 rgba(0,0,0,0);";
	@conf public static String sts_css_opened="transition-duration:1s;transition-timing-function:ease;padding:1em;background:#fed;box-shadow:0 0 .5em rgba(0,0,0,.5);";
	@conf public static String sts_css_closed="transition-duration:1s;transition-timing-function:ease;padding:1em;color:rgba(255,255,255,.5);box-shadow:0 0 0 rgba(0,0,0,0);";

	private static final long serialVersionUID=1;
}
