package bob;

import b.a;
import b.xwriter;

public class root extends a{
	static final long serialVersionUID=3;
	public table t;

	public void to(final xwriter x) throws Throwable{
		x.style();
		x.css("table.f","margin-left:auto;margin-right:auto");
		x.css("table.f tr:first-child","border:0;border-bottom:1px solid green;border-top:1px solid #070");
		x.css("table.f tr:last-child","border:0;border-bottom:1px solid #040");
		x.css("table.f th","padding:.5em;text-align:left;background:#fefefe;color:black;border-bottom:1px solid green");
		x.css("table.f td","padding:.5em;vertical-align:middle;border-left:1px dotted #ccc;border-bottom:1px dotted #ccc");
		x.css("table.f td:first-child","border-left:0");
		x.css(t.q,"float:right;background:yellow;border:1px dotted #555;text-align:right;width:10em;margin-left:1em");
		x.style_();
		x.nl();
		x.divo(t);
		t.to(x);
		x.div_();
	}
}
