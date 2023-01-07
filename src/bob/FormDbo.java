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
import db.FldDbl;
import db.FldFlt;
import db.FldInt;
import db.FldLng;
import db.FldStr;
import db.FldTs;
import db.RelRef;
import db.RelRefN;

/**
 * Abstracts editing of DbObject with convenience methods for input fields and
 * write to object.
 */
public abstract class FormDbo extends Form {
	private static final long serialVersionUID = 1L;

	final private LinkedHashMap<String, DbField> dbfields = new LinkedHashMap<String, DbField>();
	final private LinkedHashMap<String, a> fields = new LinkedHashMap<String, a>();
	private transient SimpleDateFormat fmtDate;
	private transient SimpleDateFormat fmtDateTime;
	private transient NumberFormat fmtNbr;

	public FormDbo(final String objectId) {
		this(objectId, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
	}

	public FormDbo(final String objectId, final int enabledFormBits) {
		super(objectId, enabledFormBits);
	}

	@Override
	public a child(final String id) {
		final a e = super.child(id);
		if (e != null)
			return e;
		return fields.get(id);
	}

	final protected void beginForm(final xwriter x) {
		x.table("f").nl();
	}

	protected abstract DbObject createObject();

	final protected void endForm(final xwriter x) {
		x.table_().nl();
	}

	final protected void focus(final xwriter x, final DbField f) {
		focus(x, f.getName());
	}

	final protected void focus(final xwriter x, final String field) {
		x.script().xfocus(getElem(field)).script_();
	}

	final protected String formatDate(final Timestamp ts) {
		if (ts == null)
			return "";
		if (fmtDate == null) {
			fmtDate = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
		}
		return fmtDate.format(ts);
	}

	final protected String formatDateTime(final Timestamp ts) {
		if (ts == null)
			return "";
		if (fmtDateTime == null) {
			fmtDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"); // format of input type datetime-local
		}
		return fmtDateTime.format(ts);
	}

	final protected String formatDbl(final double i) {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.format(i);
	}

	final protected String formatFlt(final float i) {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.format(i);
	}

	final protected String formatInt(final int i) {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.format(i);
	}

	final protected String formatLng(final long i) {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.format(i);
	}

	final protected boolean getBool(final DbField field) throws ParseException {
		return getBool(field.getName());
	}

	final protected boolean getBool(final String field) throws ParseException {
		return "1".equals(getStr(field));
	}

	final protected Timestamp getDate(final FldDateTime field) throws ParseException {
		return getDate(field.getName());
	}

	final protected Timestamp getDate(final String field) throws ParseException {
		return parseDate(getStr(field));
	}

	final protected Timestamp getDateTime(final FldDateTime field) throws ParseException {
		return getDateTime(field.getName());
	}

	final protected Timestamp getDateTime(final String field) throws ParseException {
		return parseDateTime(getStr(field));
	}

	final protected double getDbl(final FldDbl field) throws ParseException {
		return getDbl(field.getName());
	}

	final protected double getDbl(final String field) throws ParseException {
		return parseDbl(getStr(field));
	}

	final protected a getElem(final DbField f) {
		return getElem(f.getName());
	}

	final protected a getElem(final String field) {
		return fields.get(field);
	}

	final protected float getFlt(final FldFlt field) throws ParseException {
		return getFlt(field.getName());
	}

	final protected float getFlt(final String field) throws ParseException {
		return parseFlt(getStr(field));
	}

	final protected int getInt(final FldInt field) throws ParseException {
		return getInt(field.getName());
	}

	final protected int getInt(final String field) throws ParseException {
		return parseInt(getStr(field));
	}

	final protected long getLng(final FldLng field) throws ParseException {
		return getLng(field.getName());
	}

	final protected long getLng(final String field) throws ParseException {
		return parseLng(getStr(field));
	}

	protected abstract DbObject getObject();

	final protected int getSelectedId(final RelRef rel) {
		return getSelectedId(rel.getName());
	}

	final protected int getSelectedId(final String field) {
		final InputRelRef e = (InputRelRef) fields.get(field);
		return e.getSelectedId();
	}

	final protected Set<String> getSelectedIds(final RelRefN rel) {
		return getSelectedIds(rel.getName());
	}

	final protected Set<String> getSelectedIds(final String field) {
		final InputRelRefN e = (InputRelRefN) fields.get(field);
		return e.getSelectedIds();
	}

	final protected String getStr(final FldStr f) {
		return getStr(f.getName());
	}

	final protected String getStr(final String field) {
		final a e = fields.get(field);
		return e.str().trim();
	}

	final protected void inputBool(final xwriter x, final String label, final DbObject o, final FldBool f,
			final boolean defaultValue) {
		final boolean value = o == null ? defaultValue : ((Boolean) DbObject.getFieldValue(o, f));
		inputBool(x, label, f.getName(), null, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputBool(final xwriter x, final String label, final String field, final String styleClass,
			final boolean value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, value ? "1" : "0");
		x.inp(e, "checkbox", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputDate(final xwriter x, final String label, final DbObject o, final FldDateTime f,
			final Timestamp defaultValue) {
		final Timestamp value = o == null ? defaultValue : (Timestamp) DbObject.getFieldValue(o, f);
		inputDate(x, label, f.getName(), null, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputDate(final xwriter x, final String label, final String field, final String styleClass,
			final Timestamp value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatDate(value));
		x.inp(e, "date", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputDateTime(final xwriter x, final String label, final DbObject o, final FldDateTime f,
			final Timestamp defaultValue) {
		final Timestamp value = o == null ? defaultValue : (Timestamp) DbObject.getFieldValue(o, f);
		inputDateTime(x, label, f.getName(), null, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputDateTime(final xwriter x, final String label, final String field, final String styleClass,
			final Timestamp value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatDateTime(value));
		x.inp(e, "datetime-local", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputTimestamp(final xwriter x, final String label, final DbObject o, final FldTs f,
			final Timestamp defaultValue) {
		final Timestamp value = o == null ? defaultValue : (Timestamp) DbObject.getFieldValue(o, f);
		inputTimestamp(x, label, f.getName(), null, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputTimestamp(final xwriter x, final String label, final String field,
			final String styleClass, final Timestamp value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatDateTime(value));
		x.inp(e, "datetime-local", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputDbl(final xwriter x, final String label, final DbObject o, final FldDbl f,
			final String styleClass, final double defaultValue) {
		final double value = o == null ? defaultValue : ((Number) DbObject.getFieldValue(o, f)).doubleValue();
		inputDbl(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputDbl(final xwriter x, final String label, final String field, final String styleClass,
			final double value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatDbl(value));
		x.inp(e, null, styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputElem(final xwriter x, final String label, final DbField f, final a elem)
			throws Throwable {
		inputElem(x, label, f.getName(), elem);
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

	final protected void inputFlt(final xwriter x, final String label, final DbObject o, final FldFlt f,
			final String styleClass, final float defaultValue) {
		final float value = o == null ? defaultValue : ((Number) DbObject.getFieldValue(o, f)).floatValue();
		inputFlt(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputFlt(final xwriter x, final String label, final String field, final String styleClass,
			final float value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatFlt(value));
		x.inp(e, null, styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputInt(final xwriter x, final String label, final DbObject o, final FldInt f,
			final String styleClass, final int defaultValue) {
		final int value = o == null ? defaultValue : ((Number) DbObject.getFieldValue(o, f)).intValue();
		inputInt(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputInt(final xwriter x, final String label, final String field, final String styleClass,
			final int value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatInt(value));
		x.inp(e, null, styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputLng(final xwriter x, final String label, final DbObject o, final FldLng f,
			final String styleClass, final long defaultValue) {
		final long value = o == null ? defaultValue : ((Number) DbObject.getFieldValue(o, f)).longValue();
		inputLng(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputLng(final xwriter x, final String label, final String field, final String styleClass,
			final long value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatLng(value));
		x.inp(e, null, styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputRef(final xwriter x, final String label, final DbObject o, final RelRef rel,
			final Class<? extends ViewTable> viewTableSelectClass, final Class<? extends Form> createFormCls) {
		inputRef(x, label, rel.getName(), o, rel, viewTableSelectClass, createFormCls);
	}

	final protected void inputRef(final xwriter x, final String label, final String field, final DbObject o,
			final RelRef rel, final Class<? extends ViewTable> viewTableSelectClass,
			final Class<? extends Form> createFormCls) {
		InputRelRef e = (InputRelRef) fields.get(field);
		if (e == null) {
			e = new InputRelRef(rel, viewTableSelectClass, createFormCls);
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

	final protected void inputRefN(final xwriter x, final String label, final DbObject o, final RelRefN rel,
			final Class<? extends ViewTable> viewTableSelectClass, final Class<? extends Form> createFormCls) {
		inputRefN(x, label, rel.getName(), o, rel, viewTableSelectClass, createFormCls);
	}

	final protected void inputRefN(final xwriter x, final String label, final String field, final DbObject o,
			final RelRefN rel, final Class<? extends ViewTable> viewTableSelectClass,
			final Class<? extends Form> createFormCls) {
		InputRelRefN e = (InputRelRefN) fields.get(field);
		if (e == null) {
			e = new InputRelRefN(rel, viewTableSelectClass, createFormCls, "<br>");
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

	final protected void inputText(final xwriter x, final String label, final DbObject o, final FldStr f,
			final String styleClass, final String defaultValue) {
		final String value = o == null ? defaultValue : (String) DbObject.getFieldValue(o, f);
		inputText(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
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

	final protected void inputTextArea(final xwriter x, final String label, final DbObject o, final FldStr f,
			final String styleClass, final String defaultValue) {
		final String value = o == null ? defaultValue : (String) DbObject.getFieldValue(o, f);
		inputTextArea(x, label, f.getName(), styleClass, value);
		dbfields.put(f.getName(), f);
	}

	final protected void inputTextArea(final xwriter x, final String label, final String field, final String styleClass,
			final String value) {
		x.tr().td("lbl").p(label).p(":").td("val");
		x.inptxtarea(elemFor(field, value), styleClass).nl();
	}

	final protected Timestamp parseDate(final String s) throws ParseException {
		if (Util.isEmpty(s))
			return null;
		if (fmtDate == null) {
			fmtDate = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
		}
		return new Timestamp(fmtDate.parse(s).getTime());
	}

	final protected Timestamp parseDateTime(final String s) throws ParseException {
		if (Util.isEmpty(s))
			return null;
		if (fmtDateTime == null) {
			fmtDateTime = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm"); // format of input type datetime-local
		}
		return new Timestamp(fmtDateTime.parse(s).getTime());
	}

	final protected double parseDbl(final String s) throws ParseException {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.parse(s).doubleValue();
	}

	final protected float parseFlt(final String s) throws ParseException {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.parse(s).floatValue();
	}

	final protected int parseInt(final String s) throws ParseException {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.parse(s).intValue();
	}

	final protected long parseLng(final String s) throws ParseException {
		if (fmtNbr == null) {
			fmtNbr = NumberFormat.getNumberInstance();
		}
		return fmtNbr.parse(s).longValue();
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
			if (dbf instanceof FldFlt) {
				DbObject.setFieldValue(o, dbf, getFlt(key));
				continue;
			}
			if (dbf instanceof FldDbl) {
				DbObject.setFieldValue(o, dbf, getDbl(key));
				continue;
			}
		}
		// relations
		for (final a e : fields.values()) {
			if (e instanceof InputRelRefN) {
				final InputRelRefN r = (InputRelRefN) e;
				if (o.getClass().equals(r.rel.getFromClass())) {
					r.save(o);
				}
				continue;
			}
			if (e instanceof InputRelRef) {
				final InputRelRef r = (InputRelRef) e;
				if (o.getClass().equals(r.rel.getFromClass())) {
					r.save(o);
				}
				continue;
			}
		}
		writeToObject(o);
	}

	protected abstract void writeToObject(final DbObject obj) throws Throwable;

	private a elemFor(final String nm, final Object value) {
		a e = fields.get(nm);
		if (e == null) {
			e = new a(this, nm);
			e.set(Util.toStr(value, null));
			fields.put(nm, e);
		}
		return e;
	}
}
