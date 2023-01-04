package bob;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Set;

import b.a;
import b.xwriter;
import db.DbField;
import db.DbObject;
import db.RelRef;
import db.RelRefN;

public abstract class FormDbo extends Form {
	private static final long serialVersionUID = 1L;

	final private LinkedHashMap<String, a> fields = new LinkedHashMap<String, a>();
	private transient SimpleDateFormat fmtDate;

	public FormDbo(final String parentId, final String objectId, final int enabledFormBits) {
		super(parentId, objectId, enabledFormBits);
	}

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
		x.tr().td("lbl").p(label).p(":").td("val");
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
		x.tr().td("lbl").p(label).p(":").td("val");
		x.inptxtarea(elemFor(field, value), styleClass).nl();
	}

	final protected void inputTextArea(final xwriter x, final String label, final DbField f, final String styleClass,
			final String value) {
		inputTextArea(x, label, f.getName(), styleClass, value);
	}

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
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatDate(value));
		x.inp(e, "date", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputDate(final xwriter x, final String label, final DbField f, final String styleClass,
			final Timestamp value) {
		inputDate(x, label, f.getName(), styleClass, value);
	}

	final protected void inputRefN(final xwriter x, final String label, final String field, final DbObject o,
			final RelRefN rel, final Class<? extends ViewTable> viewTableSelectClass,
			final Class<? extends Form> createFormCls) {
		InputRefN e = (InputRefN) fields.get(field);
		if (e == null) {
			e = new InputRefN(rel, viewTableSelectClass, createFormCls, "<br>");
			e.parent(this);
			e.name(field);
			fields.put(field, e);
		}
		if (o != null) {
			e.refreshInitialIds(o);
		}
		x.tr().td("lbl").p(label).p(":").td("val");
		x.divh(e);
	}

	final protected void inputRefN(final xwriter x, final String label, final DbObject o, final RelRefN rel,
			final Class<? extends ViewTable> viewTableSelectClass, final Class<? extends Form> createFormCls) {
		inputRefN(x, label, rel.getName(), o, rel, viewTableSelectClass, createFormCls);
	}

	final protected Set<String> getSelectedIds(final String field) {
		final InputRefN e = (InputRefN) fields.get(field);
		return e.getSelectedIds();
	}

	final protected Set<String> getSelectedIds(final RelRefN rel) {
		return getSelectedIds(rel.getName());
	}

	final protected void inputRef(final xwriter x, final String label, final String field, final DbObject o,
			final RelRef rel, final Class<? extends ViewTable> viewTableSelectClass,
			final Class<? extends Form> createFormCls) {
		InputRef e = (InputRef) fields.get(field);
		if (e == null) {
			e = new InputRef(rel, viewTableSelectClass, createFormCls);
			e.parent(this);
			e.name(field);
			fields.put(field, e);
		}
		if (o != null) {
			e.refreshCurrentId(o);
		}
		x.tr().td("lbl").p(label).p(":").td("val");
		x.divh(e);
	}

	final protected void inputRef(final xwriter x, final String label, final DbObject o, final RelRef rel,
			final Class<? extends ViewTable> viewTableSelectClass, final Class<? extends Form> createFormCls) {
		inputRef(x, label, rel.getName(), o, rel, viewTableSelectClass, createFormCls);
	}

	final protected int getSelectedId(final String field) {
		final InputRef e = (InputRef) fields.get(field);
		return e.getSelectedId();
	}

	final protected int getSelectedId(final RelRef rel) {
		return getSelectedId(rel.getName());
	}

	final protected void inputElem(final xwriter x, final String label, final String field, final a elem)
			throws Throwable {
		final a e = fields.get(field);
		x.tr().td("lbl").p(label).p(":").td("val");
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

	final protected void inputElem(final xwriter x, final String label, final DbField f, final a elem)
			throws Throwable {
		inputElem(x, label, f.getName(), elem);
	}

	final protected void focus(final xwriter x, final String field) {
		x.script().xfocus(getElem(field)).script_();
	}

	final protected void focus(final xwriter x, final DbField f) {
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

	private void saveElems(final DbObject o) {
		for (final a e : fields.values()) {
			if (e instanceof InputRefN) { // ? ugly instanceof
				final InputRefN r = (InputRefN) e;
				if (o.getClass().equals(r.rel.getFromClass())) {
					r.save(o);
				}
				continue;
			}
			if (e instanceof InputRef) { // ? ugly instanceof
				final InputRef r = (InputRef) e;
				if (o.getClass().equals(r.rel.getFromClass())) {
					r.save(o);
				}
				continue;
			}
		}
	}

	@Override
	protected final void save(final xwriter x) throws Throwable {
		final DbObject o;
		if (objectId == null) { // create new
			o = createNewObject();
			objectId = Integer.toString(o.id());
		} else {
			o = getObject();
		}
		saveElems(o);

		writeToObject(o);
	}

	protected abstract DbObject createNewObject();

	protected abstract DbObject getObject();

	protected abstract void writeToObject(final DbObject obj) throws Throwable;
}
