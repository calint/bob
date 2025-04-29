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
import db.Query;
import db.test.Game;
import db.test.User;

public final class TableUserGames extends ViewTable {

    private final static long serialVersionUID = 1;

    private final int userId;

    public TableUserGames(final int userId) {
        super(null, BIT_SEARCH | BIT_SELECT | BIT_CREATE | BIT_DELETE, BIT_CLICK_ITEM,
                new TypeInfo("user game", "user games"));
        this.userId = userId;
    }

    public String getTitle() {
        return "User games";
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
            return dbo.toList();
        }
        return dbo.toList(p.getLimit());
    }

    private DbObjects getResults() {
        final Query qry = new Query();
        if (!q.is_empty()) {
            qry.and(Game.name, Query.LIKE, "%" + q.str() + "%");
        }
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, userId);
        return u.getGames().get(qry);
    }

    @Override
    protected String getIdFrom(final Object obj) {
        return Integer.toString(((Game) obj).id());
    }

    @Override
    protected void renderHeaders(final xwriter x) {
        x.th().p("Name");
    }

    @Override
    protected void renderRowCells(final xwriter x, final Object obj) {
        final Game o = (Game) obj;
        x.td();
        renderLink(x, o, o.getName());
    }

    @Override
    protected void onRowClick(final xwriter x, final String id, final String cmd) throws Throwable {
        if (cmd == null) {
            final Form f = new FormUserGame(extendIdPath(Integer.toString(userId)), id, null).init();
            super.bubble_event(x, this, f);
            return;
        }
    }

    @Override
    protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
        final Form f = new FormUserGame(extendIdPath(Integer.toString(userId)), null, initStr).init();
        super.bubble_event(x, this, f);
    }

    @Override
    protected void onActionDelete(final xwriter x) throws Throwable {
        final Set<String> sel = getSelectedIds();
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, userId);
        for (final String id : sel) {
            u.deleteGame(Integer.parseInt(id));
        }
        sel.clear();
    }

}
