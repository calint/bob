package bob.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import b.xwriter;
import bob.Action;
import bob.ViewTable;

public class TableMock extends ViewTable {
	static final long serialVersionUID = 1;

	public TableMock() {
		super(BIT_CREATE | BIT_DELETE | BIT_SELECT | BIT_SEARCH, BIT_CLICK_ITEM);
	}

	public String getTitle() {
		return "Mock files";
	}

	@Override
	protected List<?> getObjectsList() {
		final List<String> result = new ArrayList<String>();
		final String qstr = q.str().toLowerCase();
		for (final String title : DataMock.ls) {
			if (!title.toLowerCase().startsWith(qstr)) {
				continue;
			}
			result.add(title);
		}
		return result;
	}

	@Override
	protected String getIdFrom(final Object o) {
		return o.toString();
	}

	@Override
	protected String getNameFrom(final Object o) {
		return o.toString();
	}

	@Override
	protected void onActionCreate(final xwriter x, final String init_str) throws Throwable {
		final FormMock fm = new FormMock(null, null, init_str);
		super.bubble_event(x, this, fm);
	}

	@Override
	protected void onActionDelete(final xwriter x) throws Throwable {
		final Set<String> selectedIds = getSelectedIds();
		for (final String id : selectedIds) {
			DataMock.ls.remove(id);
		}
		selectedIds.clear();
	}

	@Override
	protected void onAction(final xwriter x, final Action act) throws Throwable {
		final Set<String> selectedIds = getSelectedIds();
		x.xalert(act.name() + selectedIds);
	}

	@Override
	protected void renderHeaders(final xwriter x) {
		x.th().p("Title").th().p("Created").th().p("Size");
	}

	@Override
	protected void renderRowCells(final xwriter x, final Object o) {
		x.td();
		renderLinked(x, o, o.toString());
		x.td().p("2022-12-06 15:33");
		x.td().p("12 KB");
	}

	@Override
	protected void onRowClick(final xwriter x, final String id) throws Throwable {
		final FormMock fm = new FormMock(null, id, null);
		super.bubble_event(x, this, fm);
	}
}
