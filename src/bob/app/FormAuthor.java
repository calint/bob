package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.Db;
import db.DbObject;
import db.test.Author;

public final class FormAuthor extends FormDbo {
	private static final long serialVersionUID = 1L;
	private final String initStr;

	public FormAuthor() {
		this(null, null);
	}

	public FormAuthor(final String objectId, final String initStr) {
		super(null, objectId);
		this.initStr = initStr;
	}

	public String getTitle() {
		final Author o = (Author) getObject();
		return o == null ? "New author" : o.getName();
	}

	@Override
	protected DbObject createObject() {
		return Db.currentTransaction().create(Author.class);
	}

	@Override
	protected DbObject getObject() {
		return Db.currentTransaction().get(Author.class, getObjectId());
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Author o = (Author) getObject();
		beginForm(x);
		inputText(x, "Name", Author.name, "medium", o == null ? initStr : o.getName());
		focus(x, Author.name);
		endForm(x);
	}

	@Override
	protected void writeToObject(final DbObject obj) throws Throwable {
		final Author o = (Author) obj;
		o.setName(getStr(Author.name));
	}
}
