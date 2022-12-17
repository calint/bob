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
	final protected int enabled_table_bits;

	public container ac; // actions
	public a q; // query field
	public table t; // the table

	public view_table(final int view_bits, final int table_bits) {
		super(view_bits);
		enabled_table_bits = table_bits;
		t.setTableView(this);
		if ((enabled_view_bits & BIT_CREATE) != 0) {
			ac.add(new action("create", "create"));
		}
		if ((enabled_view_bits & BIT_DELETE) != 0) {
			ac.add(new action("delete", "delete"));
		}
		final List<action> actions = getActionsList();
		if (actions == null) {
			return;
		}
		for (final action a : actions) {
			ac.add(a);
		}
	}

	@Override
	public final void to(final xwriter x) throws Throwable {
		if (!ac.elements().isEmpty()) {
			x.nl();
			x.divh(ac);
			if ((enabled_view_bits & BIT_SEARCH) == 0) {
				x.nl();
			}
		}
		if ((enabled_view_bits & BIT_SEARCH) != 0) {
			x.inpax(q, "query", this, "q", "new");
			x.is().xfocus(q).is_();
		}
		x.divh(t);
	}

	@Override
	protected final void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
		if (from instanceof action) {
			final String code = ((action) from).code();
			if ("create".equals(code) && (enabled_view_bits & BIT_CREATE) != 0) {
				onActionCreate(x, q.str());
				return;
			}
			if ("delete".equals(code) && (enabled_view_bits & BIT_DELETE) != 0) {
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
	public final void x_q(final xwriter x, final String s) throws Throwable {
		x.xu(t);
	}

	/** Callback for press enter in query field. */
	public final void x_new(final xwriter x, final String s) throws Throwable {
		if ((enabled_view_bits & BIT_CREATE) != 0) {
			onActionCreate(x, q.str());
		}
	}

	@Override
	protected final Set<String> getSelectedIds() {
		return t.getSelectedIds();
	}

//	protected void renderLinkedName(xwriter x, Object o) {
//		final String nm = getNameFrom(o);
//		renderLinked(x, o, nm);
//	}

	protected final void renderLinked(final xwriter x, final Object o, final String linkText) {
		if ((enabled_table_bits & view_table.BIT_CLICK_ITEM) != 0) {
			final String id = getIdFrom(o);
			x.ax(t, "clk " + id, linkText);
		} else {
			x.p(linkText);
		}
	}

	@Override
	protected void onActionCreate(final xwriter x, final String init_str) throws Throwable {
	}

	@Override
	protected void onActionDelete(final xwriter x) throws Throwable {
	}

	protected void renderHeaders(final xwriter x) {
	}

	protected void renderRowCells(final xwriter x, final Object o) {
	}

	protected void onRowClick(final xwriter x, final String id) throws Throwable {
	}

	public final static class table extends a {
		static final long serialVersionUID = 1;
		public container cbs; // checkboxes
		private view_table tv; // the parent
		private final HashSet<String> selectedIds = new HashSet<String>();

		public void setTableView(final view_table tv) {
			this.tv = tv;
		}

		@Override
		public void to(final xwriter x) throws Throwable {
			final List<?> ls = tv.getObjectsList();
			x.table("f").nl();
			x.tr();
			if ((tv.enabled_view_bits & view.BIT_SELECT) != 0) { // header for the checkbox
				x.th();
			}
			tv.renderHeaders(x);
			x.nl();

			cbs.elements().clear();
			for (final Object o : ls) {
				final String id = tv.getIdFrom(o);
				x.tr();
				if ((tv.enabled_view_bits & view.BIT_SELECT) != 0) { // render checkbox
					x.td();
					final checkbox cb = new checkbox(id, selectedIds.contains(id));
					// add to container where the element will get a unique name in the context.
					// the parent of the checkbox will be the container.
					cbs.add(cb);
					// checkbox now has parent and name. render it.
					cb.to(x);
				}
				tv.renderRowCells(x, o);
				x.nl();
			}
			x.table_();
		}

		@Override
		protected void bubble_event(final xwriter js, final a from, final Object o) throws Throwable {
			// event bubbled from child
			if (((tv.enabled_view_bits & view.BIT_SELECT) != 0) && (from instanceof checkbox)) {
				final String id = ((checkbox) from).getId();
				if ("checked".equals(o)) {
					selectedIds.add(id);
					return;
				}
				if ("unchecked".equals(o)) {
					selectedIds.remove(id);
					return;
				}
			}
			// event unknown by this element, bubble to parent
			super.bubble_event(js, from, o);
		}

		/** Callback for click on row. */
		public void x_clk(final xwriter x, final String s) throws Throwable {
			if ((tv.enabled_table_bits & view_table.BIT_CLICK_ITEM) != 0) {
				tv.onRowClick(x, s);
			}
		}

		protected Set<String> getSelectedIds() {
			return selectedIds;
		}
	}
}
