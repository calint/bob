package bob.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import b.xwriter;
import bob.Action;
import bob.FormDbo;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.test.Author;
import db.test.Book;
import db.test.Category;
import db.test.DataText;
import db.test.Publisher;

public final class FormBook2 extends FormDbo {
	private static final long serialVersionUID = 1L;
	private final String initStr;

	public FormBook2() {
		this(null, null);
	}

	public FormBook2(final String objectId, final String initStr) {
		super(null, objectId, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
		this.initStr = initStr;
	}

	public String getTitle() {
		final Book o = (Book) getObject();
		return o == null ? "New book" : o.getName();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Book o = (Book) getObject();
		beginForm(x);
		inputText(x, "Title", Book.name, "long", o == null ? initStr : o.getName());
		focus(x, Book.name);
		inputRefN(x, "Authors", o, Book.authors, TableAuthors.class, FormAuthor.class);
		inputRef(x, "Publisher", o, Book.publisher, TablePublishers.class, FormPublisher.class);
		inputDate(x, "Published date", Book.publishedDate, "short", o == null ? null : o.getPublishedDate());
//		inputText(x, "Categories", Book.categoriesStr, "medium", o == null ? "" : o.getCategoriesStr());
		inputRefN(x, "Categories", o, Book.categories, TableCategories.class, FormCategory.class);
		inputTextArea(x, "Description", DataText.data, "large", o == null ? "" : o.getData(true).getData());
		endForm(x);
		x.ax(this, "test", "test").nl();
	}

	@Override
	protected DbObject createNewObject() {
		return Db.currentTransaction().create(Book.class);
	}

	@Override
	protected DbObject getObject() {
		return Db.currentTransaction().get(Book.class, getObjectId());
	}

	@Override
	protected void writeToObject(final DbObject obj) throws Throwable {
		final Book o = (Book) obj;
		final DbTransaction tn = Db.currentTransaction();
		o.setName(getStr(Book.name));

		final StringBuilder authorsSb = new StringBuilder(128);
		// note authors relation updated by FormDbo
		for (final DbObject ao : o.getAuthors().toList()) {
			Author a = (Author) ao;
			authorsSb.append(a.getName()).append(';');
		}
		if (authorsSb.length() > 1) {
			authorsSb.setLength(authorsSb.length() - 1);
		}
		o.setAuthorsStr(authorsSb.toString());

		// note publisher relation updated by FormDbo
		final Publisher publisher = o.getPublisher();
		o.setPublisherStr(publisher == null ? "" : publisher.getName());

		o.setPublishedDate(getDate(Book.publishedDate));

		final StringBuilder categoriesSb = new StringBuilder(128);
		// note categories relation updated by FormDbo
		for (final DbObject co : o.getCategories().toList()) {
			Category c = (Category) co;
			categoriesSb.append(c.getName()).append(';');
		}
		if (categoriesSb.length() > 1) {
			categoriesSb.setLength(categoriesSb.length() - 1);
		}
		o.setCategoriesStr(categoriesSb.toString());

		final DataText d = o.getData(true);
		d.setMeta(o.getName() + " " + o.getAuthorsStr() + " " + o.getPublisherStr() + " " + o.getCategoriesStr());
		d.setData(getStr(DataText.data));
	}

	@Override
	protected List<Action> getActionsList() {
		final List<Action> ls = new ArrayList<Action>();
		ls.add(new Action("alert me", "alert"));
		return ls;
	}

	@Override
	protected void onAction(final xwriter x, final Action act) {
		if ("alert".equals(act.code())) {
			x.xalert("alert");
			return;
		}
		super.onAction(x, act);
	}

	public void x_test(final xwriter x, final String param) throws Throwable {
		x.xu(this);
	}
}
