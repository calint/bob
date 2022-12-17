package bob;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;

public final class table extends a {
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
		if ((tv.enabled_view_bits & view_table.BIT_SELECT) != 0)
			x.th();
		x.th().p("Name");
		tv.renderHeaders(x);
		x.nl();

		cbs.elements().clear();
		for (final Object o : ls) {
			final String id = tv.getIdFrom(o);
			x.tr();
			if ((tv.enabled_view_bits & view_table.BIT_SELECT) != 0) {
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
			if ((tv.enabled_table_bits & view_table.BIT_CLICK_ITEM) != 0)
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
		if ((tv.enabled_view_bits & view_table.BIT_SELECT) != 0) {
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
		if ((tv.enabled_table_bits & view_table.BIT_CLICK_ITEM) != 0)
			tv.onRowClick(x, s);
	}
	
	protected final Set<String> getSelectedIds() {
		return selectedIds;
	}
}