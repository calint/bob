package bob.app;

import java.util.List;
import java.util.Set;

import b.xwriter;
import bob.Form;
import bob.ViewTable;
import db.Db;
import db.DbObjects;
import db.DbTransaction;
import db.Order;
import db.Query;
import db.test.Author;

public final class TableAuthors extends ViewTable {
	static final long serialVersionUID = 2;

	public TableAuthors() {
		super(null, BIT_SEARCH | BIT_CREATE | BIT_DELETE | BIT_SELECT, BIT_CLICK_ITEM,
				new TypeInfo("author", "authors"));
	}

	public String getTitle() {
		return "Authors";
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
			qry.and(Author.name, Query.LIKE, "%" + q.str() + "%");
		}
		return new DbObjects(null, Author.class, qry, new Order(Author.name));
	}

	@Override
	protected String getIdFrom(final Object o) {
		final Author bc = (Author) o;
		return Integer.toString(bc.id());
	}

	@Override
	protected void renderHeaders(final xwriter x) {
		x.th().p("Name");
		x.th().p("Books");
	}

	@Override
	protected void renderRowCells(final xwriter x, final Object o) {
		final Author bc = (Author) o;
		x.td();
		renderLink(x, bc, bc.getName());
		x.td("icn");
		renderLink(x, Integer.toString(bc.id()), "b", "<img src=/bob/link.png>");
	}

	@Override
	protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
		if (cmd == null) {
			final Form f = new FormAuthor(id, null).init();
			super.bubble_event(x, this, f);
			return;
		}
		if ("b".equals(cmd)) {
			final TableAuthor t = new TableAuthor(Integer.parseInt(id));
			super.bubble_event(x, this, t);
		}
	}

	@Override
	protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
		final Form f = new FormAuthor(null, initStr).init();
		super.bubble_event(x, this, f);
	}

	@Override
	protected void onActionDelete(final xwriter x) throws Throwable {
		final Set<String> sel = getSelectedIds();
		final DbTransaction tn = Db.currentTransaction();
		for (final String id : sel) {
			final Author o = (Author) tn.get(Author.class, id);
			tn.delete(o);
		}
		sel.clear();
	}
}
