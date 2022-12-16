package bob.elem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import b.xwriter;
import bob.action;
import bob.action_create;
import bob.action_delete;
import bob.data;
import bob.view_table;

public class table_mock extends view_table {
	static final long serialVersionUID = 1;

	public String getTitle() {
		return "Mock files";
	}

	@Override
	protected List<action> getActionsList() {
		final List<action> ls = new ArrayList<action>();
		ls.add(new action_create());
		ls.add(new action_delete());
		return ls;
	}

	@Override
	protected List<?> getObjectsList() {
		final List<String> result = new ArrayList<String>();
		final String qstr = q.str().toLowerCase();
		for (Iterator<String> i = data.ls.iterator(); i.hasNext();) {
			final String title = i.next();
			if (!title.toLowerCase().startsWith(qstr)) {
				continue;
			}
			result.add(title);
		}
		return result;
	}

	@Override
	protected String getIdFrom(Object o) {
		return o.toString();
	}

	@Override
	protected String getNameFrom(Object o) {
		return o.toString();
	}

	@Override
	protected void renderHeaders(xwriter x) {
		x.th().p("Created").th().p("Size");
	}

	@Override
	protected void renderRowCells(xwriter x, Object o) {
		x.td().p("2022-12-06 15:33");
		x.td().p("12 KB");
	}

	@Override
	protected void onRowClick(xwriter x, String id) throws Throwable {
		form_mock fm = new form_mock(null, id, null);
		super.bubble_event(x, this, fm);
	}

	@Override
	protected void onActionCreate(xwriter x, String init_str) throws Throwable {
		form_mock fm = new form_mock(null, null, init_str);
		super.bubble_event(x, this, fm);
	}

	@Override
	protected void onActionDelete(xwriter x) throws Throwable {
		final Set<String> selectedIds = getSelectedIds();
		for (String id : selectedIds) {
			data.ls.remove(id);
		}
		selectedIds.clear();
	}

	@Override
	protected void onAction(xwriter x, action act) throws Throwable {
		final Set<String> selectedIds = getSelectedIds();
		x.xalert(act.name() + selectedIds);
	}
}
