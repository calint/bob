package bob.app;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import b.a;
import b.xwriter;
import bob.Action;
import bob.Form;
import bob.Util;
import db.Db;
import db.DbField;
import db.DbTransaction;
import db.test.Book;
import db.test.DataText;

public class FormBook2 extends Form {
	private static final long serialVersionUID = 2L;

	final private LinkedHashMap<String, a> fields = new LinkedHashMap<String, a>();

	final protected void beginForm(final xwriter x) {
		x.table("f").nl();
	}

	final protected void endForm(final xwriter x) {
		x.table_().nl();
	}

	final protected void inputText(final xwriter x, final String label, final DbField f, final String styleClass,
			final String value) {
		final String nm = f.getName();
		a e = fields.get(nm);
		if (e == null) {
			e = new a(this, nm);
			e.set(value);
			fields.put(nm, e);
		}
		x.tr().td("lbl").p(label).p(": ").td("val").inptxt(e, styleClass, null, this, "sc").nl();
	}

	final protected void inputElem(final xwriter x, final DbField f, final a elem) throws Throwable {
		final String nm = f.getName();
		final a e = fields.get(nm);
		if (e == null) {
			elem.parent(this);
			elem.name(nm);
			fields.put(nm, elem);
			elem.to(x);
		} else {
			if (elem != e)
				throw new RuntimeException("expected same element");
			e.to(x);
		}
	}

	public void focus(final xwriter x, final DbField f) {
		x.script().xfocus(getElem(f)).script_();
	}

	final protected String getStr(final DbField f) {
		final a e = fields.get(f.getName());
		return e.str();
	}

	final protected a getElem(final DbField f) {
		return fields.get(f.getName());
	}

	@Override
	public a child(final String id) {
		final a e = super.child(id);
		if (e != null)
			return e;
		return fields.get(id);
	}

	// ----------------------------------------------------------------------------------------
	private final String initStr;

	public FormBook2(final String objectId, final String initStr) {
		super(null, objectId, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
		this.initStr = initStr;
	}

	public String getTitle() {
		return Util.toStr(getStr(Book.name), "New book");
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Book o = (Book) (objectId == null ? null : Db.currentTransaction().get(Book.class, objectId));
		beginForm(x);
		inputText(x, "Title", Book.name, "long", o == null ? initStr : o.getName());
		focus(x, Book.name);
		inputText(x, "Authors", Book.authors, "long", o == null ? "" : o.getAuthors());
		inputText(x, "Publisher", Book.publisher, "medium", o == null ? "" : o.getPublisher());
		inputText(x, "Published date", Book.publishedDate, "short",
				o == null ? "" : Util.toStr(o.getPublishedDate(), ""));
		endForm(x);
		x.ax(this, "test", "test").nl();
	}

	public void x_test(final xwriter x, final String param) throws Throwable {
		x.xu(this);
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
		o.setAuthors(getStr(Book.authors));
		o.setPublisher(getStr(Book.publisher));
		final String pd = getStr(Book.publishedDate);
		if (!Util.isEmpty(pd)) {
			o.setPublishedDate(Timestamp.valueOf(pd));
		}

		final DataText d = o.getData(true);
		d.setMeta(o.getName() + " " + o.getAuthors() + " " + o.getPublisher());
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
}
