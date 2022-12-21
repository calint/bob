package bob.app;

import java.sql.Timestamp;

import b.a;
import b.xwriter;
import bob.Form;
import bob.Util;
import db.Db;
import db.DbTransaction;
import db.test.Book;
import db.test.DataText;

public class FormBook extends Form {
	private static final long serialVersionUID = 1L;

	public a name;
	public a authors;
	public a publisher;
	public a publishedDate;

	public FormBook(final String objectId, final String initStr) {
		super(null, objectId, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
		final Book o = (Book) (objectId == null ? null : Db.currentTransaction().get(Book.class, objectId));
		name.set(o == null ? initStr : o.getName());
		authors.set(o == null ? "" : o.getAuthors());
		publisher.set(o == null ? "" : o.getPublisher());
		publishedDate.set(o == null ? "" : Util.toStr(o.getPublishedDate(), ""));
	}

	public String getTitle() {
		return Util.toStr(name.str(), "New book");
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		x.p("title: ").inptxt(name, this, "sc").nl();
		x.script().xfocus(name).script_();
		x.p("authors: ").inptxt(authors, this, "sc").nl();
		x.p("publisher: ").inptxt(publisher, this, "sc").nl();
		x.p("publishedDate: ").inptxt(publishedDate, this, "sc").nl();
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
		o.setName(name.str());
		o.setAuthors(authors.str());
		o.setPublisher(publisher.str());
		if (!publishedDate.is_empty()) {
			o.setPublishedDate(Timestamp.valueOf(publishedDate.str()));
		}

		final DataText d = o.getData(true);
		d.setMeta(o.getName() + " " + o.getAuthors() + " " + o.getPublisher());
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

	public final void x_sc(final xwriter x, final String param) throws Throwable {
		saveAndClose(x);
	}
}
