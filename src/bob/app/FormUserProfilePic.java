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
import db.test.File;
import db.test.User;

public final class FormUserProfilePic extends FormDbo {

    private final static long serialVersionUID = 1;

    public FormUserProfilePic() {
        this(null, null, null);
    }

    public FormUserProfilePic(final IdPath idPath, final String objectId, final String initStr) {
        super(idPath, File.class, objectId, initStr);
    }

    public String title() {
        return Util.toStr(getStr(File.name), "New file");
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        final File o = (File) getObject();
        beginForm(x);
        inputText(x, "Name", o, File.name, getInitStr(), "medium");
        focus(x, File.name);
        endForm(x);
    }

    @Override
    protected DbObject createObject() {
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, idPath().current());
        return u.getProfilePic(true);
    }

    @Override
    protected DbObject getObject() {
        final String oid = objectId();
        if (oid == null) {
            return null;
        }
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, idPath().current());
        return u.getProfilePic(false);
    }

    @Override
    protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
        // `FormDbo` writes the input fields to the `DbObject`
    }

}
