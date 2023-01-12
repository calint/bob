package bob.app;

import b.xwriter;
import bob.FormDbo;
import bob.Util;
import db.DbObject;
import db.test.File;

public final class FormFile extends FormDbo {
	private static final long serialVersionUID = 1L;

	public FormFile() {
		this(null, null);
	}

	public FormFile(final String objectId, final String initStr) {
		super(File.class, objectId, initStr);
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
	protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
		// FormDbo writes the input fields to the DbObject
	}
}
