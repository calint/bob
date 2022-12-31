package bob.app;

import java.util.List;
import java.util.Set;

import b.xwriter;
import bob.View;
import bob.ViewTable;
import db.Db;
import db.DbObjects;
import db.DbTransaction;
import db.Order;
import db.Query;
import db.test.Category;

public final class TableCategories extends ViewTable {
	static final long serialVersionUID = 2;

	public TableCategories() {
		super(BIT_SEARCH | BIT_SELECT | BIT_CREATE | BIT_DELETE, BIT_CLICK_ITEM);
	}

	public String getTitle() {
		return "Categories";
	}

	@Override
	protected View.TypeInfo getTypeInfo() {
		return new View.TypeInfo("category", "categories");
	}

	@Override
	protected int getObjectsPerPageCount() {
		return 50;
	}

	@Override
	protected int getObjectsCount() {
		return getResults().getCount();
	}

	@Override
	protected List<?> getObjectsList() {
		final DbObjects dbo = getResults();
		if (!p.isEnabled()) // if no paging
			return dbo.toList();

		return dbo.toList(p.getLimit());
	}

	protected DbObjects getResults() {
		final Query qry = new Query();
		if (!q.is_empty()) {
			qry.and(Category.name, Query.LIKE, "%" + q.str() + "%");
		}
		return new DbObjects(null, Category.class, qry, new Order(Category.name));
	}

	@Override
	protected String getIdFrom(final Object o) {
		final Category bc = (Category) o;
		return Integer.toString(bc.id());
	}

	@Override
	protected void renderHeaders(final xwriter x) {
		x.th().p("Name").th().p("Books");
	}

	@Override
	protected void renderRowCells(final xwriter x, final Object o) {
		final Category bc = (Category) o;
		x.td();
		renderLink(x, bc, bc.getName());
		x.td("icn");
		renderLink(x, Integer.toString(bc.id()), "b", "<img src=/bob/link.png>");
	}

	@Override
	protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
		if (cmd == null) {
			final FormCategory f = new FormCategory(id, null);
			super.bubble_event(x, this, f);
			return;
		}
		if ("b".equals(cmd)) {
			final TableCategory tbc = new TableCategory(Integer.parseInt(id));
			super.bubble_event(x, this, tbc);
		}
	}

	@Override
	protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
		final FormCategory f = new FormCategory(null, initStr);
		super.bubble_event(x, this, f);
	}

	@Override
	protected void onActionDelete(final xwriter x) throws Throwable {
		final Set<String> sel = getSelectedIds();
		final DbTransaction tn = Db.currentTransaction();
		for (final String id : sel) {
			final Category o = (Category) tn.get(Category.class, id);
			tn.delete(o);
		}
		sel.clear();
	}

}
