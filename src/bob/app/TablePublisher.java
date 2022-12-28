package bob.app;

import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;
import bob.Action;
import bob.View;
import bob.ViewTable;
import db.Db;
import db.DbObjects;
import db.DbTransaction;
import db.Query;
import db.test.Book;
import db.test.DataText;
import db.test.Publisher;

public class TablePublisher extends ViewTable {
	static final long serialVersionUID = 2;
	public a title;
	public a id;

	private final int publisherId;

	public TablePublisher(final int publisherId) {
		super(BIT_SEARCH | BIT_SELECT | BIT_CREATE | BIT_DELETE, BIT_CLICK_ITEM);
		this.publisherId = publisherId;
	}

	public String getTitle() {
		final Publisher bc = (Publisher) Db.currentTransaction().get(Publisher.class, publisherId);
		return bc.getName();
	}

	@Override
	protected View.TypeInfo getTypeInfo() {
		return new View.TypeInfo("book", "books");
	}

	@Override
	protected boolean hasMoreSearchSection() {
		return true;
	}

	@Override
	protected void renderMoreSearchSection(final xwriter x) {
		x.p("Id: ").inptxt(id, "nbr").p(' ');
		x.p("Exact book title: ").inptxt(title, "small");
	}

	@Override
	protected boolean isInifiniteScroll() {
		return false;
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
		if (!p.isEnabled()) // if no paging
			return dbo.toList();

		return dbo.toList(p.getLimit());
	}

	protected DbObjects getResults() {
		final Query qry = new Query();
		if (!q.is_empty()) {
			qry.and(DataText.ft, q.str()).and(Book.data);
		}
		if (!title.is_empty()) {
			qry.and(Book.name, Query.EQ, title.str());
		}
		if (!id.is_empty()) {
			qry.and(Book.class, id.toint());
		}
		qry.and(Book.publisher).and(Publisher.class, publisherId);
		return new DbObjects(null, Book.class, qry, null);
	}

	@Override
	protected String getIdFrom(final Object o) {
		final Book b = (Book) o;
		return Integer.toString(b.id());
	}

	@Override
	protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
		final FormBook2 fm = new FormBook2(null, initStr);
		super.bubble_event(x, this, fm);
	}

	@Override
	protected void onActionDelete(final xwriter x) throws Throwable {
		final Set<String> sel = getSelectedIds();
		final DbTransaction tn = Db.currentTransaction();
		for (final String id : sel) {
			final Book b = (Book) tn.get(Book.class, Integer.parseInt(id));
			tn.delete(b);
		}
		sel.clear();
	}

	@Override
	protected void onAction(final xwriter x, final Action act) throws Throwable {
		final Set<String> selectedIds = getSelectedIds();
		x.xalert(act.name() + selectedIds);
	}

	@Override
	protected void renderHeaders(final xwriter x) {
		x.th().p("Id").th().p("Title").th().p("Author");
	}

	@Override
	protected void renderRowCells(final xwriter x, final Object o) {
		final Book b = (Book) o;
		x.td("nbr").p(b.id());
		x.td();
		renderLinked(x, b, b.getName());
		x.td().p(b.getAuthors().replaceAll("\\s*;\\s*", "<br>"));
	}

	@Override
	protected void onRowClick(final xwriter x, final String id) throws Throwable {
//		final FormBook f = new FormBook(id, q.str());
		final FormBook2 f = new FormBook2(id, q.str());
		super.bubble_event(x, this, f);
	}
}
