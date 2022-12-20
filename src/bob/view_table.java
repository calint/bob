package bob;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;

public abstract class view_table extends view {
	private static final long serialVersionUID = 2;

	public final static int BIT_CLICK_ITEM = 1;
	/** The actions that are enabled in the table. */
	final protected int enabled_table_bits;

	public container ac; // actions
	public a q; // query field
	public table t;
	public paging p;
	private TypeInfo typeInfo; // the name and plural of the object type

	public view_table(final int view_bits, final int table_bits) {
		super(view_bits);
		enabled_table_bits = table_bits;
		t.setTableView(this);
		p.setTableView(this);
		if ((enabled_view_bits & BIT_CREATE) != 0) {
			ac.add(new action("create", "create"));
		}
		if ((enabled_view_bits & BIT_DELETE) != 0) {
			ac.add(new action("delete", "delete"));
		}
		typeInfo = getTypeInfo();
		final List<action> actions = getActionsList();
		if (actions == null)
			return;
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
			x.script().xfocus(q).script_();
		} else {
			x.nl();
		}
		x.divh(t);
		if (!p.isEnabled())
			return;
		x.nl();
		x.divh(p);
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
		if (from == p) { // event from pager
			x.xu(t, p); // update table and paging element
			x.xfocus(q); // focus on query field
			x.xscrollToTop(); // scroll to top of page
			return;
		}
		// event unknown by this element
		super.bubble_event(x, from, o);
	}

	/** Callback for change in query field. */
	public final void x_q(final xwriter x, final String s) throws Throwable {
		if (p.isEnabled()) {
			p.setPage(1); // reset the page
			x.xu(p); // update paging
		}
		x.xu(t); // update table
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

	protected final void renderLinked(final xwriter x, final Object o, final String linkText) {
		if ((enabled_table_bits & view_table.BIT_CLICK_ITEM) != 0) {
			final String id = getIdFrom(o);
			x.ax(t, "clk " + id, linkText);
		} else {
			x.p(linkText);
		}
	}

	protected TypeInfo getTypeInfo() {
		return new TypeInfo("object", "objects");
	}

	@Override
	protected int getObjectsPerPageCount() {
		return 0;
	}

	@Override
	protected int getObjectsCount() {
		return 0;
	}

	@Override
	protected void onActionCreate(final xwriter x, final String init_str) throws Throwable {
	}

	@Override
	protected void onActionDelete(final xwriter x) throws Throwable {
	}

	@Override
	protected void onAction(final xwriter x, final action act) throws Throwable {
	}

	protected void renderHeaders(final xwriter x) {
	}

	protected void renderRowCells(final xwriter x, final Object o) {
	}

	protected void onRowClick(final xwriter x, final String id) throws Throwable {
	}

	public final static class TypeInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		protected String name;
		protected String namePlural;

		public TypeInfo(final String name, final String namePlural) {
			this.name = name;
			this.namePlural = namePlural;
		}
	}

	public final static class paging extends a {
		private static final long serialVersionUID = 1L;
		private int currentPage; // page starting at 0
		private int objectsPerPage;
		private int npages;
		private view_table tv;
		public a pg; // current page

		public void setTableView(final view_table tv) {
			this.tv = tv;
			objectsPerPage = tv.getObjectsPerPageCount();
			pg.set(currentPage + 1);
		}

		@Override
		public void to(final xwriter x) throws Throwable {
			final int count = tv.getObjectsCount();
			if (count < objectsPerPage) {
				npages = 1;
			} else {
				npages = count / objectsPerPage;
			}
			x.p(count);
			x.p(' ');
			if (count == 1) {
				x.p(tv.typeInfo.name);
			} else {
				x.p(tv.typeInfo.namePlural);
			}
			x.p(". Page ");
			x.inp(pg, null, "nbr center", null, null, this, "p", null, null);
			x.p(" of ");
			x.p(npages);
			x.p(". ");

			if (currentPage != 0) {
				x.ax(this, "pg prv", "Previous");
				x.p(" ");
			}
			if (currentPage < npages - 1) {
				x.ax(this, "pg nxt", "Next");
			}
		}

		public void x_pg(final xwriter x, final String param) throws Throwable {
			if ("prv".equals(param)) {
				currentPage--;
				if (currentPage < 0) {
					currentPage = 0;
				}
			}
			if ("nxt".equals(param)) {
				currentPage++;
				if (currentPage >= npages) {
					currentPage = npages - 1;
				}
			}
			pg.set(currentPage + 1);
			super.bubble_event(x);
		}

		public void x_p(final xwriter x, final String param) throws Throwable {
			final int n;
			try {
				n = pg.toint();
			} catch (final Throwable t) {
				x.xu(tv.p);
				x.xfocus(pg);
				x.xalert("Enter a page number.");
				return;
			}
			currentPage = n - 1;
			if (currentPage < 0) {
				currentPage = 0;
				pg.set(currentPage + 1);
			} else if (currentPage >= npages) {
				currentPage = npages - 1;
				pg.set(currentPage + 1);
			}
			super.bubble_event(x);
		}

//
//		public int getCurrentPage() {
//			return currentPage;
//		}
//
//		public int getObjectsPerPageCount() {
//			return getObjectsPerPageCount();
//		}

		public int getLimitStart() {
			return currentPage * objectsPerPage;
		}

		public int getLimitCount() {
			return objectsPerPage;
		}

		public boolean isEnabled() {
			return objectsPerPage != 0;
		}

		public void setPage(final int page) {
			currentPage = page - 1;
			pg.set(page);
		}
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
			if ((tv.enabled_view_bits & view.BIT_SELECT) != 0 && from instanceof checkbox) {
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
