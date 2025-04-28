// reviewed: 2024-08-05
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
import db.Query;
import db.test.File;
import db.test.User;

public final class TableUserFiles extends ViewTable {

    private static final long serialVersionUID = 1;

    private final int userId;

    public TableUserFiles(final int userId) {
        super(null, BIT_SEARCH | BIT_SELECT | BIT_CREATE | BIT_DELETE, BIT_CLICK_ITEM,
                new TypeInfo("user file", "user files"));
        this.userId = userId;
    }

    public String getTitle() {
        return "User files";
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
            qry.and(File.name, Query.LIKE, "%" + q.str() + "%");
        }
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, userId);
        return u.getFiles().get(qry);
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
        x.p(Util.formatSizeInBytes(o.getSizeBytes()));
    }

    @Override
    protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
        if (cmd == null) {
            final Form f = new FormUserFile(makeExtendedIdPath(Integer.toString(userId)), id, null).init();
            super.bubble_event(x, this, f);
            return;
        }
    }

    @Override
    protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
        final Form f = new FormUserFile(makeExtendedIdPath(Integer.toString(userId)), null, initStr).init();
        super.bubble_event(x, this, f);
    }

    @Override
    protected void onActionDelete(final xwriter x) throws Throwable {
        final Set<String> sel = getSelectedIds();
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, userId);
        for (final String id : sel) {
            u.deleteFile(Integer.parseInt(id));
        }
        sel.clear();
    }

}
