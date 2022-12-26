package bob;

import b.a;
import b.xwriter;

public final class Checkbox extends a {
	static final long serialVersionUID = 1;
//	final private static String off = "◻";
//	final private static String on = "▣";
	final private static String off = "0";
	final private static String on = "1";
	final private String id;

	public Checkbox(final String id, final boolean checked) {
		set(checked ? on : off);
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	public void to(final xwriter x) throws Throwable {
		final String cb = "0".equals(str()) ? "s" : "u";
		x.inp(this, "checkbox", null, null, str(), this, cb, this, cb);
//		x.spano(this);
//		if (on.equals(str())) {
//			x.ax(this, "u", on);
//		} else {
//			x.ax(this, "s", off);
//		}
//		x.span_();
	}

	public void x_s(final xwriter x, final String param) throws Throwable {
		set(on);
		bubble_event(x, this, "checked"); // bubble event
		x.xuo(this); // replace outer html element with this id with the output of this element
	}

	public void x_u(final xwriter x, final String param) throws Throwable {
		set(off);
		bubble_event(x, this, "unchecked"); // bubble event
		x.xuo(this); // replace outer html element with this id with the output of this element
	}
}
