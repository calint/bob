package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.Db;
import db.DbTransaction;
import db.test.Publisher;

public class FormPublisher extends FormDbo {
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
		final Publisher o = (Publisher) (objectId == null ? null
				: Db.currentTransaction().get(Publisher.class, objectId));
		return o == null ? "New publisher" : o.getName();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Publisher o = (Publisher) (objectId == null ? null
				: Db.currentTransaction().get(Publisher.class, objectId));
		beginForm(x);
		inputText(x, "Name", Publisher.name, "medium", o == null ? initStr : o.getName());
		focus(x, Publisher.name);
		endForm(x);
	}

	@Override
	protected void save(final xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final Publisher o;
		if (objectId == null) { // create new
			o = (Publisher) tn.create(Publisher.class);
			objectId = Integer.toString(o.id());
		} else {
			o = (Publisher) tn.get(Publisher.class, objectId);
		}
		o.setName(getStr(Publisher.name));
	}
}
