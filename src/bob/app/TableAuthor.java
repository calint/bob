package bob.app;

import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;
import bob.Action;
import bob.Util;
import bob.View;
import bob.ViewTable;
import db.Db;
import db.DbObjects;
import db.DbTransaction;
import db.Query;
import db.test.Author;
import db.test.Book;
import db.test.DataText;

public class TableAuthor extends ViewTable {
	static final long serialVersionUID = 2;
	public a title;
	public a id;

	private final int authorId;

	public TableAuthor(final int authorId) {
		super(BIT_SEARCH | BIT_SELECT | BIT_CREATE | BIT_DELETE, BIT_CLICK_ITEM);
		this.authorId = authorId;
	}

	public String getTitle() {
		final Author bc = (Author) Db.currentTransaction().get(Author.class, authorId);
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
		qry.and(Book.authors).and(Author.class, authorId);
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
		renderLink(x, b, b.getName());
		x.td();
		final String a = b.getAuthorsStr();
		if (!Util.isEmpty(a)) {
			final String[] ca = a.split("\\s*;\\s*");
			int i = 0;
			for (final String s : ca) {
				renderLink(x, s, "a", s);
				i++;
				if (i < ca.length) {
					x.p("<br>");
				}
			}
		}
	}

	@Override
	protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
		if (cmd == null) {
			final FormBook2 f = new FormBook2(id, q.str());
			super.bubble_event(x, this, f);
			return;
		}
		if ("a".equals(cmd)) { // category link
			final Author o = (Author) Db.currentTransaction()
					.get(Author.class, new Query(Author.name, Query.EQ, id), null, null).get(0);
			final TableAuthor t = new TableAuthor(o.id());
			super.bubble_event(x, this, t);
		}
	}
}