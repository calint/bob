package bob.app;

import java.util.List;
import java.util.Set;

import b.xwriter;
import bob.Form;
import bob.Util;
import bob.ViewTable;
import db.Db;
import db.DbObjects;
import db.DbTransaction;
import db.Order;
import db.Query;
import db.test.File;

public final class TableFiles extends ViewTable {
	static final long serialVersionUID = 1;

	public TableFiles() {
		super(null, BIT_SEARCH | BIT_CREATE | BIT_DELETE | BIT_SELECT, BIT_CLICK_ITEM, new TypeInfo("file", "files"));
	}

	public String getTitle() {
		return "Files";
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
			qry.and(File.name, Query.LIKE, "%" + q.str() + "%");
		}
		return new DbObjects(null, File.class, qry, new Order(File.name));
	}

	@Override
	protected String getIdFrom(final Object obj) {
		final File o = (File) obj;
		return Integer.toString(o.id());
	}

	@Override
	protected void renderHeaders(final xwriter x) {
		x.th().p("Name");
		x.th().p("Bytes");
	}

	@Override
	protected void renderRowCells(final xwriter x, final Object obj) {
		final File o = (File) obj;
		x.td();
		renderLink(x, o, o.getName());
		x.td("nbr");
		x.p(Util.formatSizeInBytes(o.getSize_B()));
	}

	@Override
	protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
		if (cmd == null) {
			final Form f = new FormFile(null, id, null).init();
			super.bubble_event(x, this, f);
			return;
		}
	}

	@Override
	protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
		final Form f = new FormFile(null, null, initStr).init();
		super.bubble_event(x, this, f);
	}

	@Override
	protected void onActionDelete(final xwriter x) throws Throwable {
		final Set<String> sel = getSelectedIds();
		final DbTransaction tn = Db.currentTransaction();
		for (final String id : sel) {
			final File o = (File) tn.get(File.class, id);
			tn.delete(o);
		}
		sel.clear();
	}
}
