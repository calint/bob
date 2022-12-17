package bob;

import b.a;
import b.xwriter;

public class action extends a {
	private static final long serialVersionUID = 1L;
	private final String code;

	public action() {
		this("", "");
	}

	public action(String text, String code) {
		set(text);
		this.code = code;
	}

	public final String code() {
		return code;
	}

	@Override
	public final void to(xwriter x) throws Throwable {
		x.ax(this, null, str());
	}

	public final void x_(xwriter x, String param) throws Throwable {
		super.bubble_event(x, this, str());
	}
}
