package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.DbObject;
import db.test.Publisher;

public final class FormPublisher extends FormDbo {
	private static final long serialVersionUID = 1L;

	public FormPublisher() {
		this(null, null);
	}

	public FormPublisher(final String objectId, final String initStr) {
		super(Publisher.class, objectId, initStr);
	}

	public String getTitle() {
		final Publisher o = (Publisher) getObject();
		return o == null ? "New publisher" : o.getName();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Publisher o = (Publisher) getObject();
		beginForm(x);
		inputText(x, "Name", o, Publisher.name, getInitStr(), "medium");
		focus(x, Publisher.name);
		endForm(x);
	}

	@Override
	protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
//		final Publisher o = (Publisher) obj;
//		o.setName(getStr(Publisher.name));
	}
}
