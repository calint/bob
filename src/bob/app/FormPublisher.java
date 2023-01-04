package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.Db;
import db.DbObject;
import db.test.Publisher;

public final class FormPublisher extends FormDbo {
	private static final long serialVersionUID = 1L;
	private final String initStr;

	public FormPublisher() {
		this(null, null);
	}

	public FormPublisher(final String objectId, final String initStr) {
		super(null, objectId, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
		this.initStr = initStr;
	}

	public String getTitle() {
		final Publisher o = (Publisher) getObject();
		return o == null ? "New publisher" : o.getName();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Publisher o = (Publisher) getObject();
		beginForm(x);
		inputText(x, "Name", Publisher.name, "medium", o == null ? initStr : o.getName());
		focus(x, Publisher.name);
		endForm(x);
	}

	@Override
	protected DbObject createNewObject() {
		return Db.currentTransaction().create(Publisher.class);
	}

	@Override
	protected DbObject getObject() {
		return Db.currentTransaction().get(Publisher.class, getObjectId());
	}

	@Override
	protected void writeToObject(final DbObject obj) throws Throwable {
		final Publisher o = (Publisher) obj;
		o.setName(getStr(Publisher.name));
	}
}
