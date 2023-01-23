package bob.app;

import java.util.List;

import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.test.File;

public final class FormFile extends FormFileAbstract {
	private static final long serialVersionUID = 1L;

	public FormFile() {
		this(null, null, null);
	}

	public FormFile(final List<String> idPath, final String objectId, final String initStr) {
		super(idPath, objectId, initStr);
	}

	@Override
	protected DbObject createObject() {
		final DbTransaction tn = Db.currentTransaction();
		return tn.create(File.class);
	}

	@Override
	protected DbObject getObject() {
		final String oid = getObjectId();
		if (oid == null)
			return null;
		final DbTransaction tn = Db.currentTransaction();
		return tn.get(File.class, oid);
	}
}
