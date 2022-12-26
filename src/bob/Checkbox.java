package bob;

import b.a;
import b.xwriter;

public final class Checkbox extends a {
	static final long serialVersionUID = 1;
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
		x.inp(this, "checkbox", null, null, str(), null, null, this, null);
	}

	public void x_(final xwriter x, final String param) throws Throwable {
		bubble_event(x, this, on.equals(str()) ? "checked" : "unchecked"); // bubble event
		x.xucb(this); // update checkbox
	}
}
