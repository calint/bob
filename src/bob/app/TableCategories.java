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
import db.test.Category;

public final class TableCategories extends ViewTable {

    private final static long serialVersionUID = 1;

    public TableCategories() {
        super(new TypeInfo("category", "categories"), null, BIT_SEARCH | BIT_SELECT | BIT_CREATE | BIT_DELETE,
                BIT_RENDER_LINKED_ITEM);
    }

    public String title() {
        return "Categories";
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
            // if no paging
            return dbo.toList();
        }
        return dbo.toList(p.getLimit());
    }

    private DbObjects getResults() {
        final Query qry = new Query();
        if (!q.is_empty()) {
            qry.and(Category.name, Query.LIKE, "%" + q.str() + "%");
        }
        return new DbObjects(null, Category.class, qry, new Order(Category.name));
    }

    @Override
    protected String idFrom(final Object o) {
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
            final Form f = new FormCategory(id, null).init();
            super.bubble_event(x, this, f);
            return;
        }
        if ("b".equals(cmd)) {
            // category books link
            final TableCategory tbc = new TableCategory(Integer.parseInt(id));
            super.bubble_event(x, this, tbc);
        }
    }

    @Override
    protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
        final Form f = new FormCategory(null, initStr).init();
        super.bubble_event(x, this, f);
    }

    @Override
    protected void onActionDelete(final xwriter x) throws Throwable {
        final Set<String> sel = selectedIds();
        final DbTransaction tn = Db.currentTransaction();
        for (final String id : sel) {
            final Category o = (Category) tn.get(Category.class, id);
            tn.delete(o);
        }
        sel.clear();
    }

}
