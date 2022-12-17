package b.test;

import b.a;
import b.xwriter;

/** hello world */
public class t3 extends a {
	static final long serialVersionUID = 3;
	public a f;

	public t3() {
		f.set("\"'value'\"");
	}

	@Override
	public void to(final xwriter x) throws Throwable {
		x.nl().title(getClass().getName()).nl();
		x.p("1 ").is().js_x(this, "s \"'hello'\"", false).is_().nl();
		x.p("2 ").divot(f, "bytes", "background:yellow").p(" onclick=\"ui.alert('&quot;\\'hello\\'&quot;')").p("\"")
				.tagoe().p(f).div_().nl();
		x.p("3 ").divot(f, "bytes", "background:yellow").tagoe().p(f).div_().nl();
		x.p("4 ").inp(f, "text", "txt", "background:yellow", this, "test \"'hello'\"", "default \"''\"", this,
				"test ' \"chg\" '").nl();
		x.p("5 ").inp(f, "text", "txt", "background:yellow", this, "test \"'hello'\"", "default \"''\"", this, null)
				.nl();
		x.p("6 ").inp(f, "text", "txt", "background:yellow", this, "test \"'hello'\"", "default \"''\"", null, null)
				.nl();
		x.p("7 ").inp(f, "text", "txt", "background:yellow", this, "test \"'hello'\"", null, null, null).nl();
		x.p("8 ").inp(f, "text", "txt", "background:yellow", this, null, null, null, null).nl();
		x.p("9 ").inp(f, "text", "txt", "background:yellow", null, null, null, null, null).nl();
		x.p("10 ").inp(f, "text", "txt", null, null, null, null, null, null).nl();
		x.p("11 ").inp(f, "text", null, null, null, null, null, null, null).nl();
		x.p("12 ").inp(f, null, null, null, null, null, null, null, null).nl();
		x.p("13 ").inp(f, "checkbox", null, null, null, null, null, null, null).nl();
		x.p("14 ").inp(f, "checkbox", null, null, null, null, null, this, "test").nl();
		x.p("15 ").inp(f, "checkbox", null, null, null, null, null, this, null).nl();
		x.p("16 ").inp(f, "checkbox", null, null, this, "test \"'test param'\"", null, null, null).nl();
		x.p("17 ").inp(f, "checkbox", null, null, this, null, null, null, null).nl();
		x.p("18 ").inp(f, "date", null, null, this, null, null, null, null).nl();
		x.p("19 ").inp(f, "time", null, null, null, null, null, null, null).nl();
		x.p("20 ").inp(f, "number", null, null, null, null, null, null, null).nl();
		x.p("21 ").inp(f, "color", null, null, null, null, null, null, null).nl();
		x.p("22 ").inp(f, "image", null, null, null, null, null, null, null).nl();
		x.p("23 ").inp(f, "file", null, null, null, null, null, null, null).nl();
		x.p("24 ").inp(f, "search", null, null, null, null, null, null, null).nl();
		x.p("25 ").inptxtarea(f, "txtarea").nl();
		x.p("26 ").inptxtarea(f, null).nl();
		x.p("27 ").inpax(f, "txt", this, "test \"'hello'\"", "sel \"'hello'\"").nl();
		x.p("28 ").a("url\"'test'\"", "link").nl();
		x.nl();
	}

	public void x_test(final xwriter x, final String param) {
		System.out.println("test param{" + param + "}");
	}

	public void x_sel(final xwriter x, final String param) {
		System.out.println("sel param{" + param + "}");
	}
}
