//
// reviewed: 2024-08-05
//           2025-04-29
//
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
import db.test.Publisher;

public final class TablePublishers extends ViewTable {

    private final static long serialVersionUID = 1;

    public TablePublishers() {
        super(null, BIT_SEARCH | BIT_CREATE | BIT_DELETE | BIT_SELECT, BIT_CLICK_ITEM,
                new TypeInfo("publisher", "publishers"));
    }

    public String getTitle() {
        return "Publishers";
    }

    @Override
    protected int objectsPerPageCount() {
        return 50;
    }

    @Override
    protected int objectsCount() {
        return getResults().getCount();
    }

    @Override
    protected List<?> objectsList() {
        final DbObjects dbo = getResults();
        if (!p.isEnabled()) {
            return dbo.toList();
        }
        return dbo.toList(p.getLimit());
    }

    private DbObjects getResults() {
        final Query qry = new Query();
        if (!q.is_empty()) {
            qry.and(Publisher.name, Query.LIKE, "%" + q.str() + "%");
        }
        return new DbObjects(null, Publisher.class, qry, new Order(Publisher.name));
    }

    @Override
    protected String idFrom(final Object o) {
        final Publisher bc = (Publisher) o;
        return Integer.toString(bc.id());
    }

    @Override
    protected void renderHeaders(final xwriter x) {
        x.th().p("Name").th().p("Books");
    }

    @Override
    protected void renderRowCells(final xwriter x, final Object o) {
        final Publisher po = (Publisher) o;
        x.td();
        renderLink(x, po, po.getName());
        x.td("icn");
        renderLink(x, Integer.toString(po.id()), "b", "<img src=/bob/link.png>");
    }

    @Override
    protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
        if (cmd == null) {
            final Form f = new FormPublisher(id, null).init();
            super.bubble_event(x, this, f);
            return;
        }
        if ("b".equals(cmd)) {
            // publisher books link
            final TablePublisher tp = new TablePublisher(Integer.parseInt(id));
            super.bubble_event(x, this, tp);
        }
    }

    @Override
    protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
        final Form f = new FormPublisher(null, initStr).init();
        super.bubble_event(x, this, f);
    }

    @Override
    protected void onActionDelete(final xwriter x) throws Throwable {
        final Set<String> sel = selectedIds();
        final DbTransaction tn = Db.currentTransaction();
        for (final String id : sel) {
            final Publisher o = (Publisher) tn.get(Publisher.class, id);
            tn.delete(o);
        }
        sel.clear();
    }

}
