package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.DbObject;
import db.test.Category;

public final class FormCategory extends FormDbo {
	private static final long serialVersionUID = 1L;

	public FormCategory() {
		this(null, null);
	}

	public FormCategory(final String objectId, final String initStr) {
		super(Category.class, objectId, initStr);
	}

	public String getTitle() {
		final Category o = (Category) getObject();
		return o == null ? "New category" : o.getName();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Category o = (Category) getObject();
		beginForm(x);
		inputText(x, "Name", o, Category.name, getInitStr(), "medium");
		focus(x, Category.name);
		endForm(x);
	}

	@Override
	protected void writeToObject(final xwriter x, final DbObject obj) throws Throwable {
//		final Category o = (Category) obj;
//		o.setName(getStr(Category.name));
	}
}
