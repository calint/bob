//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import java.util.ArrayList;
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
import db.test.DataText;

/**
 * Demonstration of a more advanced CRUD pattern displaying the author books
 * with additional search fields.
 */
public final class TableAuthor extends ViewTable {

    private final static long serialVersionUID = 1;

    public a title;
    public a id;

    private final int authorId;

    public TableAuthor(final int authorId) {
        super(null, BIT_SEARCH | BIT_SELECT | BIT_CREATE | BIT_DELETE, BIT_CLICK_ITEM, new TypeInfo("book", "books"));
        this.authorId = authorId;
    }

    public String getTitle() {
        final Author o = (Author) Db.currentTransaction().get(Author.class, authorId);
        return o.getName();
    }

    /** This view has additional search fields. */
    @Override
    protected boolean hasMoreSearchSection() {
        return true;
    }

    /** Custom renderer of additional fields. */
    @Override
    protected void renderMoreSearchSection(final xwriter x) {
        // more search fields that calls parent `x_q` on enter pressed
        x.p("Id: ").inptxt(id, "nbr", this, "q").focus(id).spc();
        x.p("Exact book title: ").inptxt(title, "small", this, "q");
    }

    /** Clears and updates additional search fields. */
    @Override
    protected void clearMoreSearchSection(xwriter x) throws Throwable {
        id.set("");
        x.xu(id);

        title.set("");
        x.xu(title);
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

    /** Helper because results are used at `getObjectsCount` and `getObjects` */
    private DbObjects getResults() {
        final Query qry = new Query();
        qry.and(Book.authors).and(Author.class, authorId);
        if (!q.is_empty()) {
            qry.and(DataText.ft, q.str()).and(Book.data);
        }
        if (!title.is_empty()) {
            qry.and(Book.name, Query.EQ, title.str());
        }
        if (!id.is_empty()) {
            qry.and(Book.class, id.toint());
        }
        return new DbObjects(null, Book.class, qry, null);
    }

    /** Give framework the id of object in context to create links. */
    @Override
    protected String getIdFrom(final Object o) {
        final Book b = (Book) o;
        return Integer.toString(b.id());
    }

    /** On create event make editor form of books. */
    @Override
    protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
        final Form f = new FormBook2(null, initStr).init();
        super.bubble_event(x, this, f);
    }

    /** On delete selected books. */
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
    protected List<Action> getActionsList() {
        final List<Action> actions = new ArrayList<>();
        actions.add(new Action("display selected book ids", "dsbi"));
        return actions;
    }

    /** Dummy response to any custom action. */
    @Override
    protected void onAction(final xwriter x, final Action act) throws Throwable {
        final Set<String> selectedIds = getSelectedIds();
        x.xalert("action code: " + act.code() + " selected ids: " + selectedIds);
    }

    /** Table headers. */
    @Override
    protected void renderHeaders(final xwriter x) {
        x.th().p("Id").th().p("Title").th().p("Author");
    }

    /** Render row in table using supplied object in context. */
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
                // render link with code `a` to be handled in `onRowClick` if clicked
                renderLink(x, s, "a", s);
                i++;
                if (i < ca.length) {
                    x.p("<br>");
                }
            }
        }
    }

    /** React to click on link in row. */
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
