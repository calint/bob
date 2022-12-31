package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.Db;
import db.DbTransaction;
import db.test.Author;

public final class FormAuthor extends FormDbo {
	private static final long serialVersionUID = 1L;
	private final String initStr;

	public FormAuthor() {
		this(null, null);
	}

	public FormAuthor(final String objectId, final String initStr) {
		super(null, objectId, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
		this.initStr = initStr;
	}

	public String getTitle() {
		final Author o = (Author) (objectId == null ? null : Db.currentTransaction().get(Author.class, objectId));
		return o == null ? "New author" : o.getName();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Author o = (Author) (objectId == null ? null : Db.currentTransaction().get(Author.class, objectId));
		beginForm(x);
		inputText(x, "Name", Author.name, "medium", o == null ? initStr : o.getName());
		focus(x, Author.name);
		endForm(x);
	}

	@Override
	protected void save(final xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final Author o;
		if (objectId == null) { // create new
			o = (Author) tn.create(Author.class);
			objectId = Integer.toString(o.id());
		} else {
			o = (Author) tn.get(Author.class, objectId);
		}
		o.setName(getStr(Author.name));
	}
}
