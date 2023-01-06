package bob;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import b.a;
import b.xwriter;
import db.DbField;
import db.DbObject;
import db.FldBool;
import db.FldDateTime;
import db.FldInt;
import db.FldLng;
import db.FldStr;
import db.FldTs;
import db.RelRef;
import db.RelRefN;

public abstract class FormDbo extends Form {
	private static final long serialVersionUID = 1L;

	final private LinkedHashMap<String, a> fields = new LinkedHashMap<String, a>();
	final private LinkedHashMap<String, DbField> dbfields = new LinkedHashMap<String, DbField>();
	private transient SimpleDateFormat fmtDate;
	private transient SimpleDateFormat fmtDateTime;
	private transient NumberFormat fmtNbr;

	public FormDbo(final String parentId, final String objectId, final int enabledFormBits) {
		super(parentId, objectId, enabledFormBits);
	}

	public FormDbo(final String parentId, final String objectId) {
		this(parentId, objectId, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
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
		dbfields.put(f.getName(), f);
	}

	final protected void inputTextArea(final xwriter x, final String label, final String field, final String styleClass,
			final String value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		x.inptxtarea(elemFor(field, value), styleClass).nl();
	}

	final protected void inputTextArea(final xwriter x, final String label, final DbField f, final String styleClass,
			final String value) {
		inputTextArea(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
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

	final protected String formatDateTime(final Timestamp ts) {
		if (ts == null)
			return "";
		if (fmtDateTime == null) {
			fmtDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		}
		return fmtDateTime.format(ts);
	}

	final protected Timestamp parseDateTime(final String s) throws ParseException {
		if (Util.isEmpty(s))
			return null;
		if (fmtDateTime == null) {
			fmtDateTime = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");
		}
		return new Timestamp(fmtDateTime.parse(s).getTime());
	}

	final protected String formatInt(final int i) {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.format(i);
	}

	final protected int parseInt(final String s) throws ParseException {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.parse(s).intValue();
	}

	final protected String formatLng(final long i) {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.format(i);
	}

	final protected long parseLng(final String s) throws ParseException {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.parse(s).longValue();
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
		dbfields.put(f.getName(), f);
	}

	final protected void inputDateTime(final xwriter x, final String label, final String field, final String styleClass,
			final Timestamp value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatDateTime(value));
		x.inp(e, "datetime-local", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputDateTime(final xwriter x, final String label, final DbField f, final String styleClass,
			final Timestamp value) {
		inputDateTime(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputInt(final xwriter x, final String label, final String field, final String styleClass,
			final int value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatInt(value));
		x.inp(e, null, styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputInt(final xwriter x, final String label, final DbField f, final String styleClass,
			final int value) {
		inputInt(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputLng(final xwriter x, final String label, final String field, final String styleClass,
			final long value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatLng(value));
		x.inp(e, null, styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputLng(final xwriter x, final String label, final DbField f, final String styleClass,
			final long value) {
		inputLng(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputBool(final xwriter x, final String label, final String field, final String styleClass,
			final boolean value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, value ? "1" : "0");
		x.inp(e, "checkbox", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputBool(final xwriter x, final String label, final DbField f, final String styleClass,
			final boolean value) {
		inputBool(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
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
		return e.str().trim();
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

	final protected Timestamp getDateTime(final String field) throws ParseException {
		final a e = fields.get(field);
		return parseDateTime(e.str());
	}

	final protected Timestamp getDateTime(final DbField field) throws ParseException {
		return getDateTime(field.getName());
	}

	final protected int getInt(final String field) throws ParseException {
		final a e = fields.get(field);
		return parseInt(e.str());
	}

	final protected int getInt(final DbField field) throws ParseException {
		return getInt(field.getName());
	}

	final protected long getLng(final String field) throws ParseException {
		final a e = fields.get(field);
		return parseLng(e.str());
	}

	final protected long getLng(final DbField field) throws ParseException {
		return getLng(field.getName());
	}

	final protected boolean getBool(final String field) throws ParseException {
		final a e = fields.get(field);
		return "1".equals(e.str());
	}

	final protected boolean getBool(final DbField field) throws ParseException {
		return getBool(field.getName());
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

	@Override
	protected final void save(final xwriter x) throws Throwable {
		final DbObject o;
		if (objectId == null) { // create new
			o = createObject();
			objectId = Integer.toString(o.id());
		} else {
			o = getObject();
		}
		// ? ugly instanceof chain. separation of concerns ok.
		// elements that know how to write to objects
		for (final Map.Entry<String, DbField> me : dbfields.entrySet()) {
			final DbField dbf = me.getValue();
			if (!o.getClass().equals(dbf.getDeclaringClass())) {
				continue;
			}
			final String key = me.getKey();
			if (dbf instanceof FldStr) {
				DbObject.setFieldValue(o, dbf, getStr(key));
				continue;
			}
			if (dbf instanceof FldDateTime) {
				Timestamp ts;
				try {
					ts = getDateTime(key);
				} catch (final ParseException ok) {
					ts = getDate(key);
				}
				DbObject.setFieldValue(o, dbf, ts);
				continue;
			}
			if (dbf instanceof FldTs) {
				Timestamp ts;
				try {
					ts = getDateTime(key);
				} catch (final ParseException ok) {
					ts = getDate(key);
				}
				DbObject.setFieldValue(o, dbf, ts);
				continue;
			}
			if (dbf instanceof FldInt) {
				DbObject.setFieldValue(o, dbf, getInt(key));
				continue;
			}
			if (dbf instanceof FldLng) {
				DbObject.setFieldValue(o, dbf, getLng(key));
				continue;
			}
			if (dbf instanceof FldBool) {
				DbObject.setFieldValue(o, dbf, getBool(key));
				continue;
			}
		}
		// relations
		for (final a e : fields.values()) {
			if (e instanceof InputRefN) {
				final InputRefN r = (InputRefN) e;
				if (o.getClass().equals(r.rel.getFromClass())) {
					r.save(o);
				}
				continue;
			}
			if (e instanceof InputRef) {
				final InputRef r = (InputRef) e;
				if (o.getClass().equals(r.rel.getFromClass())) {
					r.save(o);
				}
				continue;
			}
		}
		writeToObject(o);
	}

	protected abstract DbObject createObject();

	protected abstract DbObject getObject();

	protected abstract void writeToObject(final DbObject obj) throws Throwable;
}
