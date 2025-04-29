//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import b.xwriter;
import bob.Action;
import bob.Form;
import bob.ViewTable;
import db.Db;
import db.DbObject;
import db.DbObjects;
import db.DbTransaction;
import db.Order;
import db.Query;
import db.test.User;

public final class TableUsers extends ViewTable {

    private final static long serialVersionUID = 1;

    public TableUsers() {
        super(null, BIT_SEARCH | BIT_CREATE | BIT_DELETE | BIT_SELECT, BIT_CLICK_ITEM, new TypeInfo("user", "users"));
    }

    public String getTitle() {
        return "Users";
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
        if (!p.isEnabled()) {
            return dbo.toList();
        }
        return dbo.toList(p.getLimit());
    }

    private DbObjects getResults() {
        final Query qry = new Query();
        if (!q.is_empty()) {
            qry.and(User.ixFt, q.str());
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
            final Form f = new FormUser(id, null).init();
            super.bubble_event(x, this, f);
            return;
        }
        if ("f".equals(cmd)) {
            // user files link
            final TableUserFiles t = new TableUserFiles(Integer.parseInt(id));
            super.bubble_event(x, this, t);
        }
        if ("g".equals(cmd)) {
            // user games link
            final TableUserGames t = new TableUserGames(Integer.parseInt(id));
            super.bubble_event(x, this, t);
        }
    }

    @Override
    protected void onActionCreate(final xwriter x, final String initStr) throws Throwable {
        final Form f = new FormUser(null, initStr).init();
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

    @Override
    protected List<Action> getActionsList() {
        final ArrayList<Action> ls = new ArrayList<Action>();
        ls.add(new Action("delete all", "da"));
        return ls;
    }

    @Override
    protected void onAction(final xwriter x, final Action act) throws Throwable {
        if ("da".equals(act.code())) {
            // delete all
            final DbTransaction tn = Db.currentTransaction();
            for (final DbObject o : tn.get(User.class, null, null, null)) {
                tn.delete(o);
            }
            return;
        }
        super.onAction(x, act);
    }

}
