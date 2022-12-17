package bob;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;

public abstract class view_table extends view {
	static final long serialVersionUID = 1;
	
	public final static int BIT_CLICK_ITEM = 1;
	/** The actions that are enabled in the table. */
	protected int enabled_table_bits;

	public container ans; // actions
	public a q; // query field
	public table t; // the table

	public final static class table extends a {
		static final long serialVersionUID = 1;
		public container cbs; // checkboxes
		private view_table tv; // the parent
		private final HashSet<String> selectedIds = new HashSet<String>();

		public void setTableView(view_table tv) {
			this.tv = tv;
		}

		public void to(final xwriter x) throws Throwable {
			final List<?> ls = tv.getObjectsList();
			x.table("f").nl();
			x.tr();
			if ((tv.enabled_view_bits & BIT_SELECT) != 0)
				x.th();
			x.th().p("Name");
			tv.renderHeaders(x);
			x.nl();

			cbs.elements().clear();
			for (final Object o : ls) {
				final String id = tv.getIdFrom(o);
				x.tr();
				if ((tv.enabled_view_bits & BIT_SELECT) != 0) {
					x.td();
					final checkbox cb = new checkbox(id, selectedIds.contains(id));
					// add to container where the element will get a name unique in the context.
					// the parent of the checkbox will be the container.
					cbs.add(cb);
					// checkbox now has parent and name. render it.
					cb.to(x);
				}
				x.td();
				final String nm = tv.getNameFrom(o);
				if ((tv.enabled_table_bits & BIT_CLICK_ITEM) != 0)
					x.ax(this, "clk " + id, nm);
				else
					x.p(nm);
				tv.renderRowCells(x, o);
				x.nl();
			}
			x.table_();
		}

		protected void bubble_event(xwriter js, a from, Object o) throws Throwable {
			// event bubbled from child
			if ((tv.enabled_view_bits & BIT_SELECT) != 0) {
				if (from instanceof checkbox) {
					final String id = ((checkbox) from).getId();
					if ("checked".equals(o)) {
//					System.out.println("selected: "+id);
						selectedIds.add(id);
						return;
					} else if ("unchecked".equals(o)) {
//					System.out.println("unselected: "+id);
						selectedIds.remove(id);
						return;
					}
				}
			}
			// event unknown by this element, bubble to parent
			super.bubble_event(js, from, o);
		}

		/** Callback for click on row. */
		public void x_clk(xwriter x, String s) throws Throwable {
			tv.onRowClick(x, s);
		}
	}

	public view_table(int view_bits, int table_bits) {
		super(view_bits);
		enabled_table_bits = table_bits;
		t.setTableView(this);
		if ((enabled_view_bits & BIT_CREATE) != 0)
			ans.add(new action("create", "create"));
		if ((enabled_view_bits & BIT_DELETE) != 0)
			ans.add(new action("delete", "delete"));
		final List<action> actions = getActionsList();
		if (actions == null)
			return;
		for (action a : actions) {
			ans.add(a);
		}
	}

	@Override
	public final void to(final xwriter x) throws Throwable {
		x.style();
		x.css("table.f", "margin-left:auto;margin-right:auto;text-align:left");
		x.css("table.f tr:first-child", "border:0;border-bottom:1px solid green;border-top:1px solid #070");
		x.css("table.f tr:last-child", "border:0;border-bottom:1px solid #040");
		x.css("table.f th",
				"padding:.5em;text-align:left;background:#fefefe;color:black;border-bottom:1px solid green");
		x.css("table.f td",
				"padding:.5em;vertical-align:middle;border-left:1px dotted #ccc;border-bottom:1px dotted #ccc");
		x.css("table.f td:first-child", "border-left:0");
		x.css(q, "background:yellow;border:1px dotted #555;width:13em;margin:1em;padding:.2em");
		x.style_();
		if (!ans.elements().isEmpty()) {
			x.divh(ans);
			if ((enabled_view_bits & BIT_SEARCH) == 0) {
				x.nl();
			}
		}
//		x.ax(this, "up", "••");
		if ((enabled_view_bits & BIT_SEARCH) != 0) {
			x.inpax(q, null, this, "q", "new");
			x.is().xfocus(q).is_();
		}
		x.divh(t);
	}

	@Override
	protected final void bubble_event(xwriter x, a from, Object o) throws Throwable {
		if (from instanceof action) {
			final String code = ((action) from).code();
			if ("create".equals(code)) {
				onActionCreate(x, q.str());
				return;
			} else if ("delete".equals(code)) {
				if (getSelectedIds().isEmpty()) {
					x.xalert("No items selected.");
					return;
				}
				onActionDelete(x);
				x.xu(t);
				x.xfocus(q);
				return;

			}
			onAction(x, (action) from);
			return;
		}
		// event unknown by this element
		super.bubble_event(x, from, o);
	}

	/** Callback for change in query field. */
	public final void x_q(xwriter x, String s) throws Throwable {
		x.xu(t);
	}

	/** Callback for press enter in query field. */
	public final void x_new(xwriter x, String s) throws Throwable {
		onActionCreate(x, q.str());
	}

	@Override
	protected final Set<String> getSelectedIds() {
		return t.selectedIds;
	}

	protected abstract void renderHeaders(xwriter x);

	protected abstract void renderRowCells(xwriter x, Object o);

	protected abstract void onRowClick(xwriter x, String id) throws Throwable;
}
