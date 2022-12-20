package bob.app;

import java.sql.Timestamp;

import b.a;
import b.xwriter;
import bob.form;
import bob.util;
import db.Db;
import db.DbTransaction;
import db.test.Book;
import db.test.DataText;

public class form_book extends form {
	private static final long serialVersionUID = 1L;

	public a name;
	public a authors;
	public a publisher;
	public a publishedDate;

	public form_book(final String object_id, final String init_str) {
		super(null, object_id, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
		final Book o = (Book) (object_id == null ? null
				: Db.currentTransaction().get(Book.class, Integer.parseInt(object_id)));
		name.set(object_id == null ? init_str : o.getName());
		authors.set(object_id == null ? "" : o.getAuthors());
		publisher.set(object_id == null ? "" : o.getPublisher());
		publishedDate.set(object_id == null ? "" : util.str(o.getPublishedDate(), ""));
	}

	public String getTitle() {
		return util.str(name.str(), "New book");
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		x.p("title: ").inptxt(name, this, "sc").nl();
		x.script().xfocus(name).script_();
		x.p("authors: ").inptxt(authors, this, "sc").nl();
		x.p("publisher: ").inptxt(publisher, this, "sc").nl();
		x.p("publishedDate: ").inptxt(publishedDate, this, "sc").nl();
	}

//	@Override
//	protected List<action> getActionsList() {
//		final List<action> ls = new ArrayList<action>();
//		ls.add(new action("alert me", "alert"));
//		return ls;
//	}
//
//	@Override
//	protected void onAction(final xwriter x, final action act) {
//		if ("alert".equals(act.code())) {
//			x.xalert("alert");
//			return;
//		}
//		super.onAction(x, act);
//	}

	@Override
	protected void save(final xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final Book o;
		if (object_id == null) { // create new
			o = (Book) tn.create(Book.class);
		} else {
			o = (Book) tn.get(Book.class, Integer.parseInt(object_id));
		}
		o.setName(name.str());
		o.setAuthors(authors.str());
		o.setPublisher(publisher.str());
		if (!publishedDate.is_empty())
			o.setPublishedDate(Timestamp.valueOf(publishedDate.str()));

		final DataText d = o.getData(true);
		d.setMeta(o.getName() + " " + o.getAuthors() + " " + o.getPublisher());
	}

	public final void x_sc(final xwriter x, final String param) throws Throwable {
		saveAndClose(x);
	}
}
