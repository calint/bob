// reviewed: 2024-08-05
package bob.app;

import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;
import bob.Action;
import bob.Form;
import bob.Util;
import bob.ViewTable;
import db.Db;
import db.DbObjects;
import db.DbTransaction;
import db.Query;
import db.test.Author;
import db.test.Book;
import db.test.Category;
import db.test.DataText;

public final class TableCategory extends ViewTable {
    private static final long serialVersionUID = 1;

    public a title;
    public a id;

    private final int categoryId;

    public TableCategory(final int categoryId) {
        super(null, BIT_SEARCH | BIT_SELECT | BIT_CREATE | BIT_DELETE, BIT_CLICK_ITEM, new TypeInfo("book", "books"));
        this.categoryId = categoryId;
    }

    public String getTitle() {
        final Category o = (Category) Db.currentTransaction().get(Category.class, categoryId);
        return o.getName();
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
        if (!p.isEnabled()) {
            // if no paging
            return dbo.toList();
        }
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
        qry.and(Book.categories).and(Category.class, categoryId);
        return new DbObjects(null, Book.class, qry, null);
    }

    @Override
    protected String getIdFrom(final Object o) {
        final Book b = (Book) o;
        return Integer.toString(b.id());
    }

    @Override
    protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
        final Form f = new FormBook2(null, initStr).init();
        super.bubble_event(x, this, f);
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
        final String c = b.getAuthorsStr();
        if (!Util.isEmpty(c)) {
            final String[] ca = c.split("\\s*;\\s*");
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
            final Form f = new FormBook2(id, q.str()).init();
            super.bubble_event(x, this, f);
            return;
        }
        if ("a".equals(cmd)) {
            // author link
            final Author o = (Author) Db.currentTransaction()
                    .get(Author.class, new Query(Author.name, Query.EQ, id), null, null).get(0);
            final TableAuthor t = new TableAuthor(o.id());
            super.bubble_event(x, this, t);
        }
    }
}
