//
// reviewed: 2024-08-05
//           2025-04-29
//
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

    private final static long serialVersionUID = 1;

    public TableUserFiles(final IdPath idPath) {
        super(new TypeInfo("user file", "user files"), idPath, BIT_SEARCH | BIT_SELECT | BIT_CREATE | BIT_DELETE,
                BIT_RENDER_LINKED_ITEM);
    }

    public String title() {
        return "User files";
    }

    @Override
    protected int objectsPerPageCount() {
        return 20;
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
            qry.and(File.name, Query.LIKE, "%" + q.str() + "%");
        }
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, idPath().current());
        return u.getFiles().get(qry);
    }

    @Override
    protected String idFrom(final Object obj) {
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
            final Form f = new FormUserFile(idPath(), id, null).init();
            super.bubble_event(x, this, f);
            return;
        }
    }

    @Override
    protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
        final Form f = new FormUserFile(idPath(), null, initStr).init();
        super.bubble_event(x, this, f);
    }

    @Override
    protected void onActionDelete(final xwriter x) throws Throwable {
        final Set<String> sel = selectedIds();
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, idPath().current());
        for (final String id : sel) {
            u.deleteFile(Integer.parseInt(id));
        }
        sel.clear();
    }

}
