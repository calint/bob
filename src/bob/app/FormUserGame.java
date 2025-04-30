//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import b.xwriter;
import bob.FormDbo;
import bob.Util;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.test.Game;
import db.test.User;

public final class FormUserGame extends FormDbo {

    private final static long serialVersionUID = 1;

    public FormUserGame() {
        this(null, null, null);
    }

    public FormUserGame(final IdPath idPath, final String objectId, final String initStr) {
        super(idPath, Game.class, objectId, initStr);
    }

    public String title() {
        return Util.toStr(str(Game.name), "New game");
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        final Game o = (Game) object();
        beginForm(x);
        inputText(x, "Name", o, Game.name, initString(), "medium");
        focus(x, Game.name);
        endForm(x);
    }

    @Override
    protected DbObject createObject() {
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, idPath().current());
        return u.createGame();
    }

    @Override
    protected DbObject object() {
        final String oid = objectId();
        if (oid == null) {
            return null;
        }
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, idPath().current());
        return u.getGames().get(oid);
    }

    @Override
    protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
        // `FormDbo` writes the input fields to the `DbObject`
    }

}
