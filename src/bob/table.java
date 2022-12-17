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

	@Override
	public void to(final xwriter x) throws Throwable {
		final List<?> ls = tv.getObjectsList();
		x.table("f").nl();
		x.tr();
		if ((tv.enabled_view_bits & view_table.BIT_SELECT) != 0) { // header for the checkbox
			x.th();
		}
		tv.renderHeaders(x);
		x.nl();

		cbs.elements().clear();
		for (final Object o : ls) {
			final String id = tv.getIdFrom(o);
			x.tr();
			if ((tv.enabled_view_bits & view_table.BIT_SELECT) != 0) { // render checkbox
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
	protected void bubble_event(xwriter js, a from, Object o) throws Throwable {
		// event bubbled from child
		if ((tv.enabled_view_bits & view_table.BIT_SELECT) != 0) {
			if (from instanceof checkbox) {
				final String id = ((checkbox) from).getId();
				if ("checked".equals(o)) {
					selectedIds.add(id);
					return;
				} else if ("unchecked".equals(o)) {
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