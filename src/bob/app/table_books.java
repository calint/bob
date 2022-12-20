package bob.app;

import java.util.List;
import java.util.Set;

import b.xwriter;
import bob.action;
import bob.view_table;
import db.Db;
import db.DbObjects;
import db.DbTransaction;
import db.Limit;
import db.Query;
import db.test.Book;
import db.test.DataText;

public class table_books extends view_table {
	static final long serialVersionUID = 1;

	public table_books() {
		super(BIT_SEARCH | BIT_SELECT | BIT_CREATE | BIT_DELETE, BIT_CLICK_ITEM);
	}

	public String getTitle() {
		return "Books";
	}

	@Override
	protected TypeInfo getTypeInfo() {
		return new TypeInfo("book", "books");
	}

	@Override
	protected int getObjectsPerPageCount() {
		return 20;
	}

	@Override
	protected int getObjectsCount() {
		return getResults().getCount();
	}

	@Override
	protected List<?> getObjectsList() {
		final DbObjects dbo = getResults();
		if (!p.isEnabled())
			return dbo.toList();

		return dbo.toList(new Limit(p.getLimitStart(), p.getLimitCount()));
	}

	private DbObjects getResults() {
		final Query qry;
		if (!b.b.isempty(q.str())) {
			qry = new Query(DataText.ft, q.str()).and(Book.data);
		} else {
			qry = null;
		}
		return new DbObjects(null, Book.class, qry, null);
	}

	@Override
	protected String getIdFrom(final Object o) {
		final Book b = (Book) o;
		return Integer.toString(b.id());
	}

	@Override
	protected String getNameFrom(final Object o) {
		final Book b = (Book) o;
		return b.getName();
	}

	@Override
	protected void onActionCreate(xwriter x, String init_str) throws Throwable {
	}

	@Override
	protected void onActionDelete(xwriter x) throws Throwable {
		final Set<String> sel = getSelectedIds();
		for (final String id : sel) {
			final DbTransaction tn = Db.currentTransaction();
			final Book b = (Book) tn.get(Book.class, new Query(Book.class, Integer.parseInt(id)), null, null).get(0);
			tn.delete(b);
		}
		sel.clear();
	}

	@Override
	protected void onAction(final xwriter x, final action act) throws Throwable {
		final Set<String> selectedIds = getSelectedIds();
		x.xalert(act.name() + selectedIds);
	}

	@Override
	protected void renderHeaders(final xwriter x) {
		x.th().p("Name").th().p("Author");
	}

	@Override
	protected void renderRowCells(final xwriter x, final Object o) {
		final Book b = (Book) o;
		x.td().p(b.getName()).td().p(b.getAuthors());
	}

	@Override
	protected void onRowClick(final xwriter x, final String id) throws Throwable {
//		final path p = pth.get(id);
//		if (p.isdir()) {
//			final table_books f = new table_books(p);
//			super.bubble_event(x, this, f);
//			return;
//		}
//		final form_file f = new form_file(p);
//		super.bubble_event(x, this, f);
	}
}
