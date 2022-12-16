package bob.elem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;
import bob.action;
import bob.action_create;
import bob.action_delete;
import bob.data;
import bob.view_table;

public class table_mock extends view_table {
	static final long serialVersionUID = 1;

	protected List<action> getActionsList() {
		final List<action> ls = new ArrayList<action>();
		ls.add(new action_create(null, form_mock.class));
		ls.add(new action_delete());
		return ls;
	}

	protected List<?> getList() {
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

	protected String getIdFrom(Object o) {
		return o.toString();
	}

	protected String getNameFrom(Object o) {
		return o.toString();
	}

	protected void renderHeaders(xwriter x) {
		x.th().p("Created").th().p("Size");
	}

	protected void renderRowCells(xwriter x, Object o) {
		x.td().p("2022-12-06 15:33");
		x.td().p("12 KB");
	}

	protected void onRowClick(xwriter x, String id) throws Throwable {
		form_mock fm = new form_mock(null, id, null);
		super.bubble_event(x, this, fm);
	}

	public String getTitle() {
		return "Mock files";
	}

	protected void onDelete(xwriter x, Set<String> selectedIds) throws Throwable {
		System.out.println(getClass().getName() + " " + selectedIds);
		for (String id : selectedIds) {
			data.ls.remove(id);
		}
		selectedIds.clear();
	}

}
