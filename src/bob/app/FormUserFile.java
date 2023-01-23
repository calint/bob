package bob.app;

import java.util.List;

import b.xwriter;
import bob.FormDbo;
import bob.Util;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.test.File;
import db.test.User;

public final class FormUserFile extends FormDbo {
	private static final long serialVersionUID = 1L;

	public FormUserFile() {
		this(null, null, null);
	}

	public FormUserFile(final List<String> idPath, final String objectId, final String initStr) {
		super(idPath, File.class, objectId, initStr);
	}

	public String getTitle() {
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
		final List<String> idPath = getIdPath();
		final DbTransaction tn = Db.currentTransaction();
		final User u = (User) tn.get(User.class, idPath.get(0));
		return u.createFile();
	}

	@Override
	protected DbObject getObject() {
		final String oid = getObjectId();
		if (oid == null)
			return null;
		final List<String> idPath = getIdPath();
		final DbTransaction tn = Db.currentTransaction();
		final User u = (User) tn.get(User.class, idPath.get(0));
		return (File) u.getFiles().get(oid);
	}

	@Override
	protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
		// FormDbo writes the input fields to the DbObject
	}
}
