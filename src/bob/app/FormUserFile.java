// reviewed: 2024-08-05
package bob.app;

import java.util.List;

import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.test.User;

public final class FormUserFile extends FormFileAbstract {

    private final static long serialVersionUID = 1;

    public FormUserFile() {
        this(null, null, null);
    }

    public FormUserFile(final List<String> idPath, final String objectId, final String initStr) {
        super(idPath, objectId, initStr);
    }

    @Override
    protected DbObject createObject() {
        final List<String> idPath = getIdPath();
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, idPath.get(0));
        return u.createFile();
    }

    @Override
    protected DbObject getObject() {
        final String oid = getObjectId();
        if (oid == null) {
            return null;
        }
        final List<String> idPath = getIdPath();
        final DbTransaction tn = Db.currentTransaction();
        final User u = (User) tn.get(User.class, idPath.get(0));
        return u.getFiles().get(oid);
    }

}
