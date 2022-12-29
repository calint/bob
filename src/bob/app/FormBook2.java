package bob.app;

import java.util.ArrayList;
import java.util.List;

import b.xwriter;
import bob.Action;
import bob.FormDbo;
import db.Db;
import db.DbTransaction;
import db.test.Book;
import db.test.DataText;

public class FormBook2 extends FormDbo {
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
		inputText(x, "Authors", Book.authorsStr, "long", o == null ? "" : o.getAuthorsStr());
		inputRefN(x, "Authors", o, Book.authors, null, null);
		inputText(x, "Publisher", Book.publisherStr, "medium", o == null ? "" : o.getPublisherStr());
		inputDate(x, "Published date", Book.publishedDate, "short", o == null ? null : o.getPublishedDate());
		inputText(x, "Categories", Book.categoriesStr, "medium", o == null ? "" : o.getCategoriesStr());
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
		o.setAuthorsStr(getStr(Book.authorsStr));
		o.setPublisherStr(getStr(Book.publisherStr));
		o.setPublishedDate(getDate(Book.publishedDate));
		o.setCategoriesStr(getStr(Book.categoriesStr));

		final DataText d = o.getData(true);
		d.setMeta(o.getName() + " " + o.getAuthors() + " " + o.getPublisherStr() + " " + o.getCategoriesStr());
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
