package bob.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import b.xwriter;
import bob.view_table;
import db.Db;
import db.DbClass;
import db.DbObject;

public class table_dbclasses extends view_table {
	static final long serialVersionUID = 1;

	public table_dbclasses() {
		super(BIT_SEARCH, BIT_CLICK_ITEM);
	}

	public String getTitle() {
		return "List";
	}

	@Override
	protected List<?> getObjectsList() {
		final List<DbClass> result = new ArrayList<DbClass>();
		final String query = q.str();
		for (final DbClass c : Db.getDbClasses()) {
			if (!c.getJavaClass().getName().startsWith(query)) {
				continue;
			}
			result.add(c);
		}
		Collections.sort(result, new Comparator<DbClass>() {
			public int compare(final DbClass o1, final DbClass o2) {
				return o1.getJavaClass().getName().compareToIgnoreCase(o2.getJavaClass().getName());
			}
		});
		return result;
	}

	@Override
	protected String getIdFrom(final Object o) {
		return getNameFrom(o);
	}

	@Override
	protected String getNameFrom(final Object o) {
		return ((DbClass) o).getJavaClass().getName();
	}

	@Override
	protected void renderHeaders(final xwriter x) {
		x.th().p("Name").th().p("Fields").th().p("Relations");
	}

	@Override
	protected void renderRowCells(final xwriter x, final Object o) {
		final DbClass c = (DbClass) o;
		x.td();
		renderLinked(x, o, c.getJavaClass().getName());
		x.td("ar").p(c.getDeclaredFields().size());
		x.td("ar").p(c.getDeclaredRelations().size());
	}

	@Override
	protected void onRowClick(final xwriter x, final String id) throws Throwable {
		@SuppressWarnings("unchecked")
		final Class<? extends DbObject> jc = (Class<? extends DbObject>) Class.forName(id);
		final DbClass dbc = Db.getDbClassForJavaClass(jc);
		final form_dbclass f = new form_dbclass(dbc);
		super.bubble_event(x, this, f);
	}
}