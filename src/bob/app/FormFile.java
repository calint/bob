//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.test.File;

public final class FormFile extends FormFileAbstract {

    private final static long serialVersionUID = 1;

    public FormFile() {
        this(null, null, null);
    }

    public FormFile(final IdPath idPath, final String objectId, final String initStr) {
        super(idPath, objectId, initStr);
    }

    @Override
    protected DbObject createObject() {
        final DbTransaction tn = Db.currentTransaction();
        return tn.create(File.class);
    }

    @Override
    protected DbObject getObject() {
        final String oid = objectId();
        if (oid == null) {
            return null;
        }

        final DbTransaction tn = Db.currentTransaction();
        return tn.get(File.class, oid);
    }

}
