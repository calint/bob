package bob;

import java.util.ArrayList;

import b.a;
import b.xwriter;

public final class menu extends a {
	private static final long serialVersionUID = 1L;

	private final static class item extends a {
		private static final long serialVersionUID = 1L;
		Class<? extends a> cls;
		String title;

		public item(final Class<? extends a> cls, final String title) {
			this.cls = cls;
			this.title = title;
		}

	}

	private final ArrayList<item> items = new ArrayList<item>();

	@Override
	public void to(final xwriter x) throws Throwable {
		final String id = id();
		x.tago("select").default_attrs_for_element(this).attr("onchange", "$x('" + id + " s '+this.selectedIndex)");
		x.tagoe();
		int i = 0;
		for (final item im : items) {
			x.tago("option").attr("value", i).tagoe().p(im.title);
			i++;
		}
		x.tage("select");
	}

	public void addItem(final Class<? extends a> cls, final String title) {
		items.add(new item(cls, title));
	}

	public void x_s(final xwriter x, final String param) throws Throwable {
		super.bubble_event(x, this, items.get(Integer.parseInt(param)).cls);
	}
}
