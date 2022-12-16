package bob;

import b.a;
import b.xwriter;

public abstract class action extends a {
	private static final long serialVersionUID = 1L;

	public action(String name) {
		set(name);
	}

	@Override
	public final void to(xwriter x) throws Throwable {
		x.ax(this, null, str());
	}

	public final void x_(xwriter x, String param) throws Throwable {
		super.bubble_event(x);
	}
}
