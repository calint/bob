package bob.app;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

	private a elemFor(final String nm, final Object value) {
		a e = fields.get(nm);
		if (e == null) {
			e = new a(this, nm);
			e.set(Util.toStr(value, null));
			fields.put(nm, e);
		}
		return e;
	}

	final protected void inputText(final xwriter x, final String label, final String field, final String styleClass,
			final String value) {
//		x.tr().td("lbl").p(label).p(": ").td_().td("val").inptxt(e, styleClass, null, this, "sc").td_().tr_().nl();
		x.tr().td("lbl").p(label).p(": ").td("val");
		x.inp(elemFor(field, value), null, styleClass, null, null, this, "sc", null, null).nl();
//		x.tr().td("lbl").p(label).p(": ").td("val").inptxt(e, null, null, this, "sc").nl();
//		x.inptxt(e, null, null, this, "sc");
//		x.inptxt(e, styleClass, null, this, "sc");
	}

	final protected void inputText(final xwriter x, final String label, final DbField f, final String styleClass,
			final String value) {
		inputText(x, label, f.getName(), styleClass, value);
	}

	final protected void inputTextArea(final xwriter x, final String label, final String field, final String styleClass,
			final String value) {
		x.tr().td("lbl").p(label).p(": ").td("val");
		x.inptxtarea(elemFor(field, value), styleClass).nl();
	}

	final protected void inputTextArea(final xwriter x, final String label, final DbField f, final String styleClass,
			final String value) {
		inputTextArea(x, label, f.getName(), styleClass, value);
	}

	private transient SimpleDateFormat fmtDate;

	final protected String formatDate(final Timestamp ts) {
		if (ts == null)
			return "";
		if (fmtDate == null) {
			fmtDate = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
		}
		return fmtDate.format(ts);
	}

	final protected Timestamp parseDate(final String s) throws ParseException {
		if (Util.isEmpty(s))
			return null;
		if (fmtDate == null) {
			fmtDate = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
		}
		return new Timestamp(fmtDate.parse(s).getTime());
	}

	final protected void inputDate(final xwriter x, final String label, final String field, final String styleClass,
			final Timestamp value) {
		x.tr().td("lbl").p(label).p(": ").td("val");
		final a e = elemFor(field, formatDate(value));
		x.inp(e, "date", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputDate(final xwriter x, final String label, final DbField f, final String styleClass,
			final Timestamp value) {
		inputDate(x, label, f.getName(), styleClass, value);
	}

	final protected void inputElem(final xwriter x, final String field, final a elem) throws Throwable {
		final a e = fields.get(field);
		if (e == null) {
			elem.parent(this);
			elem.name(field);
			fields.put(field, elem);
			elem.to(x);
		} else {
			if (elem != e)
				throw new RuntimeException("expected same element");
			e.to(x);
		}
	}

	final protected void inputElem(final xwriter x, final DbField f, final a elem) throws Throwable {
		inputElem(x, f.getName(), elem);
	}

	public void focus(final xwriter x, final String field) {
		x.script().xfocus(getElem(field)).script_();
	}

	public void focus(final xwriter x, final DbField f) {
		focus(x, f.getName());
	}

	final protected String getStr(final String field) {
		final a e = fields.get(field);
		return e.str();
	}

	final protected String getStr(final DbField f) {
		return getStr(f.getName());
	}

	final protected Timestamp getDate(final String field) throws ParseException {
		final a e = fields.get(field);
		return parseDate(e.str());
	}

	final protected Timestamp getDate(final DbField field) throws ParseException {
		return getDate(field.getName());
	}

	final protected a getElem(final String field) {
		return fields.get(field);
	}

	final protected a getElem(final DbField f) {
		return getElem(f.getName());
	}

	@Override
	public a child(final String id) {
		final a e = super.child(id);
		if (e != null)
			return e;
		return fields.get(id);
	}

	// ----------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------
	private final String initStr;

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
		inputText(x, "Publisher", Book.publisherStr, "medium", o == null ? "" : o.getPublisherStr());
		inputDate(x, "Published date", Book.publishedDate, "short", o == null ? null : o.getPublishedDate());
		inputText(x, "Categories", Book.categoriesStr, "medium", o == null ? "" : o.getCategoriesStr());
		inputTextArea(x, "Description", DataText.data, "large", o == null ? "" : o.getData(true).getData());
		endForm(x);
//		x.ax(this, "test", "test").nl();
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
