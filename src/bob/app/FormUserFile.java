//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.test.User;

/** Form to create/edit file attached to `User`. */
public final class FormUserFile extends FormFileAbstract {

    private final static long serialVersionUID = 1;

    public FormUserFile() {
        this(null, null, null);
    }

    public FormUserFile(final IdPath idPath, final String objectId, final String initStr) {
        super(idPath, objectId, initStr);
    }

    @Override
    protected DbObject createObject() {
        // use path of ids to navigate to object in context and create an aggregated
        // file
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, idPath().current());
        return u.createFile();
    }

    @Override
    protected DbObject getObject() {
        final String oid = objectId();
        if (oid == null) {
            return null;
        }
        // use path of ids to navigate to object in context
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, idPath().current());
        return u.getFiles().get(oid);
    }

}
