package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.Db;
import db.DbObject;
import db.test.File;

public final class FormFile extends FormDbo {
	private static final long serialVersionUID = 1L;
	private final String initStr;

	public FormFile() {
		this(null, null);
	}

	public FormFile(final String objectId, final String initStr) {
		super(objectId);
		this.initStr = initStr;
	}

	public String getTitle() {
		final File o = (File) getObject();
		return o == null ? "New file" : o.getName();
	}

	@Override
	protected DbObject createObject() {
		// created at init pattern
		return null;
	}

	@Override
	protected DbObject getObject() {
		return Db.currentTransaction().get(File.class, getObjectId());
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final File o = (File) getObject();
		beginForm(x);
		inputText(x, "Name", o, File.name, initStr, "medium");
		focus(x, File.name);
		endForm(x);
	}

	@Override
	protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
		// FormDbo writes the input fields to the DbObject
	}
}
