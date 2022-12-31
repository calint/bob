package bob.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import b.xwriter;
import bob.Action;
import bob.FormDbo;
import db.Db;
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
		final Book o = (Book) (objectId == null ? null : Db.currentTransaction().get(Book.class, objectId));
		return o == null ? "New book" : o.getName();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Book o = (Book) (objectId == null ? null : Db.currentTransaction().get(Book.class, objectId));
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
	protected void save(final xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final Book o;
		if (objectId == null) { // create new
			o = (Book) tn.create(Book.class);
			objectId = Integer.toString(o.id());
		} else {
			o = (Book) tn.get(Book.class, objectId);
		}
		o.setName(getStr(Book.name));

		final StringBuilder authorsSb = new StringBuilder(128);
		final Set<String> selectedAuthors = getSelectedIds(Book.authors);
		for (final String id : selectedAuthors) {
			final Author a = (Author) tn.get(Author.class, id);
			if (a != null) { // ? what to do with dangling references
				authorsSb.append(a.getName()).append(';');
			}
		}
		if (authorsSb.length() > 1) {
			authorsSb.setLength(authorsSb.length() - 1);
		}
		o.setAuthorsStr(authorsSb.toString());

		final int publisherId = getSelectedId(Book.publisher);
		if (publisherId != 0) {
			final Publisher publisher = (Publisher) tn.get(Publisher.class, publisherId);
			o.setPublisher(publisher);
			o.setPublisherStr(publisher.getName());
		} else {
			o.setPublisher(null);
			o.setPublisherStr("");
		}
		o.setPublishedDate(getDate(Book.publishedDate));

		final StringBuilder categoriesSb = new StringBuilder(128);
		final Set<String> selectedCategories = getSelectedIds(Book.categories);
		for (final String id : selectedCategories) {
			final Category c = (Category) tn.get(Category.class, id);
			if (c != null) { // ? what to do with dangling references
				categoriesSb.append(c.getName()).append(';');
			}
		}
		if (categoriesSb.length() > 1) {
			categoriesSb.setLength(categoriesSb.length() - 1);
		}
		o.setCategoriesStr(categoriesSb.toString());

		final DataText d = o.getData(true);
		d.setMeta(o.getName() + " " + o.getAuthorsStr() + " " + o.getPublisherStr() + " " + o.getCategoriesStr());
		d.setData(getStr(DataText.data));

		saveElems(x);
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
