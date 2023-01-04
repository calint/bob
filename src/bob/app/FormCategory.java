package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.Db;
import db.DbObject;
import db.test.Category;

public final class FormCategory extends FormDbo {
	private static final long serialVersionUID = 1L;
	private final String initStr;

	public FormCategory() {
		this(null, null);
	}

	public FormCategory(final String objectId, final String initStr) {
		super(null, objectId, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
		this.initStr = initStr;
	}

	public String getTitle() {
		final Category o = (Category) getObject();
		return o == null ? "New category" : o.getName();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Category o = (Category) getObject();
		beginForm(x);
		inputText(x, "Name", Category.name, "medium", o == null ? initStr : o.getName());
		focus(x, Category.name);
		endForm(x);
	}

	@Override
	protected DbObject getObject() {
		return Db.currentTransaction().get(Category.class, getObjectId());
	}

	@Override
	protected DbObject createNewObject() {
		return Db.currentTransaction().create(Category.class);
	}

	@Override
	protected void writeToObject(final DbObject obj) throws Throwable {
		final Category o = (Category) obj;
		o.setName(getStr(Category.name));
	}
}
