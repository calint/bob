package bob.app;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import b.a;
import b.xwriter;
import bob.action;
import bob.form;
import bob.util;
import db.Db;
import db.DbField;
import db.DbTransaction;
import db.test.Book;
import db.test.DataText;

public class form_book2 extends form {
	private static final long serialVersionUID = 2L;

	final private LinkedHashMap<String, a> fields = new LinkedHashMap<String, a>();

	final protected void begin(final xwriter x) {
		x.table("form").nl();
	}

	final protected void end(final xwriter x) {
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
		x.tr().td("lbl").p(label).p(": ").td("val").inptxt(e, this, "sc", null, styleClass).nl();
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
	private String init_str;

	public form_book2(final String object_id, final String init_str) {
		super(null, object_id, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
		this.init_str = init_str;
	}

	public String getTitle() {
		return util.tostr(getStr(Book.name), "New book");
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		final Book o = (Book) (object_id == null ? null : Db.currentTransaction().get(Book.class, object_id));
		begin(x);
		inputText(x, "Name", Book.name, "long", o == null ? init_str : o.getName());
		inputText(x, "Authors", Book.authors, "long", o == null ? "" : o.getAuthors());
		inputText(x, "Publisher", Book.publisher, "medium", o == null ? "" : o.getPublisher());
		inputText(x, "Published date", Book.publishedDate, "short",
				o == null ? "" : util.tostr(o.getPublishedDate(), ""));
		end(x);
		x.ax(this, "test", "test").nl();
	}

	public void x_test(xwriter x, String param) throws Throwable {
		x.xu(this);
	}

	@Override
	protected void save(final xwriter x) throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final Book o;
		if (object_id == null) { // create new
			o = (Book) tn.create(Book.class);
			object_id = Integer.toString(o.id());
		} else {
			o = (Book) tn.get(Book.class, object_id);
		}
		o.setName(getStr(Book.name));
		o.setAuthors(getStr(Book.authors));
		o.setPublisher(getStr(Book.publisher));
		final String pd = getStr(Book.publishedDate);
		if (!util.isempty(pd))
			o.setPublishedDate(Timestamp.valueOf(pd));

		final DataText d = o.getData(true);
		d.setMeta(o.getName() + " " + o.getAuthors() + " " + o.getPublisher());
//		x.xu(this);
	}

	@Override
	protected List<action> getActionsList() {
		final List<action> ls = new ArrayList<action>();
		ls.add(new action("alert me", "alert"));
		return ls;
	}

	@Override
	protected void onAction(final xwriter x, final action act) {
		if ("alert".equals(act.code())) {
			x.xalert("alert");
			return;
		}
		super.onAction(x, act);
	}

	public final void x_sc(final xwriter x, final String param) throws Throwable {
		saveAndClose(x);
	}
}
