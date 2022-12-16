package bob.elem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import b.xwriter;
import bob.action;
import bob.action_close;
import bob.action_create;
import bob.action_delete;
import bob.action_save;
import bob.action_saveclose;
import bob.data;
import bob.table_view;

public class mock_table extends table_view {
	static final long serialVersionUID = 1;

	protected List<action> getActionsList() {
		final List<action> ls = new ArrayList<action>();
		ls.add(new action_create());
		ls.add(new action_delete());
		ls.add(new action_saveclose());
		ls.add(new action_save());
		ls.add(new action_close());
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

	protected void onRowClick(xwriter x, String id) throws Throwable{
		mock_form mf = new mock_form(id);
		bubble_event(x, this, mf);
	}

	public String getTitle() {
		return "Mock files";
	}
}
