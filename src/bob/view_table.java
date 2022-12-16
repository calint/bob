package bob;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;

public abstract class view_table extends view {
	static final long serialVersionUID = 1;

	public final static class table extends a {
		static final long serialVersionUID = 1;
		public container cbs; // checkboxes
		private view_table tv;
		private final HashSet<String> selectedIds = new HashSet<String>();

		public void setTableView(view_table tv) {
			this.tv = tv;
		}

		public void to(final xwriter x) throws Throwable {
			final List<?> ls = tv.getObjectsList();
			x.table("f").nl();
			x.tr().th();
			x.th().p("Name");
			tv.renderHeaders(x);
			x.nl();

			cbs.elements().clear();
			for (final Object o : ls) {
				final String id = tv.getIdFrom(o);
				x.tr().td();
				final checkbox cb = new checkbox(id, selectedIds.contains(id));
				// add to container where the element will get a name unique in the context.
				// the parent of the checkbox will be the container.
				cbs.add(cb);
				// checkbox now has parent and name. render it.
				cb.to(x);
				x.td();
				x.ax(this, "clk " + id, tv.getNameFrom(o));
				tv.renderRowCells(x, o);
				x.nl();
			}
			x.table_();
		}

		protected void bubble_event(xwriter js, a from, Object o) throws Throwable {
			// event bubbled from child
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
			// event unknown by this element, bubble to parent
			super.bubble_event(js, from, o);
		}

		/** Callback for click on row. */
		public void x_clk(xwriter x, String s) throws Throwable {
			tv.onRowClick(x, s);
		}
	}

	public container ans; // actions
	public a q; // query field
	public table t; // the table

	public view_table() {
		final List<action> actions = getActionsList();
		for (action a : actions) {
			ans.add(a);
		}
		t.setTableView(this);
	}

	@Override
	public void to(final xwriter x) throws Throwable {
		x.style();
		x.css(q, "background:yellow;border:1px dotted #555;width:13em;margin:1em;padding:.2em");
		x.style_();
		x.divh(ans);
//		x.ax(this, "up", "••");
		x.inpax(q, null, this, "q", "new").p(" ");
//		x.is().p("$f('").p(q.id()).p("')").is_();
		x.is().xfocus(q).is_();
		x.divh(t);
	}

	@Override
	protected void bubble_event(xwriter x, a from, Object o) throws Throwable {
		if (from instanceof action_create) {
			onActionCreate(x, q.str());
			return;
		}
		if (from instanceof action_delete) {
			onActionDelete(x);
			x.xu(t);
			x.xfocus(q);
			return;
		}
		if (from instanceof action) {
			onAction(x, (action) from);
			return;
		}
		// event unknown by this element, bubble to parent
		super.bubble_event(x, from, o);
	}

	/** Callback for change in query field. */
	public void x_q(xwriter x, String s) throws Throwable {
		x.xu(t);
	}

	@Override
	protected Set<String> getSelectedIds() {
		return t.selectedIds;
	}
//	/** Callback for press enter in query field. */
//	public void x_new(xwriter js, String s) throws Throwable {
//	}

	protected abstract void renderHeaders(xwriter x);

	protected abstract void renderRowCells(xwriter x, Object o);

	protected abstract void onRowClick(xwriter x, String id) throws Throwable;
}
