package bob.app;

import java.util.List;

import b.xwriter;
import bob.View;
import bob.ViewTable;
import db.DbObjects;
import db.Order;
import db.Query;
import db.test.Author;

public class TableAuthors extends ViewTable {
	static final long serialVersionUID = 2;

	public TableAuthors() {
		super(BIT_SEARCH, BIT_CLICK_ITEM);
	}

	public String getTitle() {
		return "Authors";
	}

	@Override
	protected View.TypeInfo getTypeInfo() {
		return new View.TypeInfo("author", "authors");
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
	}

	@Override
	protected void renderRowCells(final xwriter x, final Object o) {
		final Author bc = (Author) o;
		x.td();
		renderLinked(x, bc, bc.getName());
	}

	@Override
	protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
		final TableAuthor t = new TableAuthor(Integer.parseInt(id));
		super.bubble_event(x, this, t);
	}
}
