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
import db.test.User;

public final class TableUsers extends ViewTable {
	static final long serialVersionUID = 1;

	public TableUsers() {
		super(BIT_SEARCH | BIT_CREATE | BIT_DELETE | BIT_SELECT, BIT_CLICK_ITEM);
	}

	public String getTitle() {
		return "Users";
	}

	@Override
	protected View.TypeInfo getTypeInfo() {
		return new View.TypeInfo("user", "users");
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
			qry.and(User.name, Query.LIKE, "%" + q.str() + "%");
		}
		return new DbObjects(null, User.class, qry, new Order(User.name));
	}

	@Override
	protected String getIdFrom(final Object obj) {
		final User o = (User) obj;
		return Integer.toString(o.id());
	}

	@Override
	protected void renderHeaders(final xwriter x) {
		x.th().p("Name");
		x.th().p("Files");
		x.th().p("Games");
	}

	@Override
	protected void renderRowCells(final xwriter x, final Object obj) {
		final User o = (User) obj;
		x.td();
		renderLink(x, o, o.getName());
		x.td("icn");
		renderLink(x, Integer.toString(o.id()), "f", "<img src=/bob/link.png>");
		x.td("icn");
		renderLink(x, Integer.toString(o.id()), "g", "<img src=/bob/link.png>");
	}

	@Override
	protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
		if (cmd == null) {
			final FormUser f = new FormUser(id, null);
			super.bubble_event(x, this, f);
			return;
		}
//		if ("f".equals(cmd)) {
//			final TableAuthor t = new TableAuthor(Integer.parseInt(id));
//			super.bubble_event(x, this, t);
//		}
	}

	@Override
	protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
		final FormUser f = new FormUser(null, initStr);
		super.bubble_event(x, this, f);
	}

	@Override
	protected void onActionDelete(final xwriter x) throws Throwable {
		final Set<String> sel = getSelectedIds();
		final DbTransaction tn = Db.currentTransaction();
		for (final String id : sel) {
			final User o = (User) tn.get(User.class, id);
			tn.delete(o);
		}
		sel.clear();
	}
}
