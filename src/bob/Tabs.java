package bob;

import java.util.ArrayList;

import b.a;
import b.xwriter;

public final class Tabs extends a {
	private static final long serialVersionUID = 1L;

	public final static class Tab extends a {
		private static final long serialVersionUID = 1L;
		private a elem;

		public Tab(final String title, final a elem) {
			this.elem = elem;
			elem.parent(this);
			elem.name("e");
			set(title);
		}

		@Override
		public a child(final String id) {
			final a e = super.child(id);
			if (e != null)
				return e;
			if ("e".equals(id))
				return elem;
			return null;
		}

		public void to(xwriter x, boolean isActive) throws Throwable {
			if (isActive) {
				x.p(str()).spc();
				return;
			}
			x.ax(this, "c", str()).spc();
		}

		public void x_c(xwriter x, String param) throws Throwable {
			super.bubble_event(x);
		}
	}

	final ArrayList<Tab> tabs = new ArrayList<Tab>();
	public a ae; // active element

	public void add(Tab t) {
		tabs.add(t);
	}

	public boolean isEmpty() {
		return tabs.isEmpty();
	}

	@Override
	public void to(xwriter x) throws Throwable {
		for (final Tab t : tabs) {
			t.to(x, t.elem == ae);
		}
		ae.to(x);
	}

	@Override
	protected void bubble_event(xwriter x, a from, Object o) throws Throwable {
		if (from instanceof Tab) { // activated tab
			final Tab t = (Tab) from;
			t.elem.replace(this, ae);
			return;
		}
		super.bubble_event(x, from, o);
	}
}
