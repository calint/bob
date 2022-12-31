package bob.app;

import b.xwriter;
import bob.FormDbo;
import db.Db;
import db.DbTransaction;
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
		final Category o = (Category) (objectId == null ? null : Db.currentTransaction().get(Category.class, objectId));
		return o == null ? "New category" : o.getName();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Category o = (Category) (objectId == null ? null : Db.currentTransaction().get(Category.class, objectId));
		beginForm(x);
		inputText(x, "Name", Category.name, "medium", o == null ? initStr : o.getName());
		focus(x, Category.name);
		endForm(x);
	}

	@Override
	protected void save(final xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final Category o;
		if (objectId == null) { // create new
			o = (Category) tn.create(Category.class);
			objectId = Integer.toString(o.id());
		} else {
			o = (Category) tn.get(Category.class, objectId);
		}
		o.setName(getStr(Category.name));
	}
}
