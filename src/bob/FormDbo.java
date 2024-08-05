// reviewed: 2024-08-05
package bob;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;
import db.Db;
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
import db.RelAgg;
import db.RelAggN;
import db.RelRef;
import db.RelRefN;

/**
 * Abstracts editing of DbObject with convenience methods for input fields and
 * write to object.
 */
public abstract class FormDbo extends Form {
	private static final long serialVersionUID = 1;

	/** Marker interface to trigger creation of object prior to rendering. */
	public interface CreateObjectAtInit {
	}

	final private Class<? extends DbObject> objCls;
	final private ArrayList<String> dbfields = new ArrayList<String>();
	final private LinkedHashMap<String, a> fields = new LinkedHashMap<String, a>();
	private transient SimpleDateFormat fmtDate;
	private transient SimpleDateFormat fmtDateTime;
	private transient NumberFormat fmtNbr;

	public FormDbo(final List<String> idPath, final Class<? extends DbObject> objCls, final String objectId,
			final String initStr) {
		this(idPath, objCls, objectId, initStr, BIT_SAVE_CLOSE | BIT_SAVE | BIT_CLOSE);
	}

	public FormDbo(final List<String> idPath, final Class<? extends DbObject> objCls, final String objectId,
			final String initStr, final int enabledFormBits) {
		super(idPath, objectId, initStr, enabledFormBits);
		this.objCls = objCls;
	}

	@Override
	public Form init() {
		if (objectId == null && this instanceof CreateObjectAtInit) {
			final DbObject o = createObject();
			objectId = Integer.toString(o.id());
		}
		return super.init();
	}

	@Override
	public a child(final String id) {
		final a e = super.child(id);
		if (e != null) {
			return e;
		}
		return fields.get(id);
	}

	final protected void beginForm(final xwriter x) {
		x.table("f").nl();
	}

	protected DbObject createObject() {
		return Db.currentTransaction().create(objCls);
	}

	final protected void endForm(final xwriter x) {
		x.table_().nl();
	}

	/** Focuses on field from "render". */
	final protected void focus(final xwriter x, final DbField f) {
		focus(x, f.getName());
	}

	/** Focuses on field from "render". */
	final protected void focus(final xwriter x, final String field) {
		x.script().xfocus(getElem(field)).script_();
	}

	final protected String formatDate(final Timestamp ts) {
		if (ts == null) {
			return "";
		}
		if (fmtDate == null) {
			fmtDate = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
		}
		return fmtDate.format(ts);
	}

	final protected String formatDateTime(final Timestamp ts) {
		if (ts == null) {
			return "";
		}
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

	final protected boolean getBool(final DbField field) {
		return getBool(field.getName());
	}

	final protected boolean getBool(final String field) {
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

	protected DbObject getObject() {
		return Db.currentTransaction().get(objCls, getObjectId());
	}

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

	final protected void includeView(final xwriter x, final String field, final View view) throws Throwable {
		final a e = fields.get(field);
		x.tr().td(2, "center");
		if (e == null) {
			view.parent(this);
			view.name(field);
			fields.put(field, view);
			view.to(x);
		} else {
			if (view != e) {
				throw new RuntimeException("expected same element");
			}
			e.to(x);
		}
	}

	final protected void inputBool(final xwriter x, final String label, final DbObject o, final FldBool f,
			final boolean defaultValue) {
		final boolean value = isNewObject() ? defaultValue : f.getBool(o);
		inputBool(x, label, f.getName(), value, null);
		dbfields.add(f.getName());
	}

	final protected void inputBool(final xwriter x, final String label, final String field, final boolean value,
			final String styleClass) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, value ? "1" : "0");
		x.inp(e, "checkbox", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputDate(final xwriter x, final String label, final DbObject o, final FldDateTime f,
			final Timestamp defaultValue) {
		final Timestamp value = isNewObject() ? defaultValue : f.getDateTime(o);
		inputDate(x, label, f.getName(), value, null);
		dbfields.add(f.getName());
	}

	final protected void inputDate(final xwriter x, final String label, final String field, final Timestamp value,
			final String styleClass) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatDate(value));
		x.inp(e, "date", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputDateTime(final xwriter x, final String label, final DbObject o, final FldDateTime f,
			final Timestamp defaultValue) {
		final Timestamp value = isNewObject() ? defaultValue : f.getDateTime(o);
		inputDateTime(x, label, f.getName(), value, null);
		dbfields.add(f.getName());
	}

	final protected void inputDateTime(final xwriter x, final String label, final String field, final Timestamp value,
			final String styleClass) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatDateTime(value));
		x.inp(e, "datetime-local", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputDbl(final xwriter x, final String label, final DbObject o, final FldDbl f,
			final double defaultValue, final String styleClass) {
		final double value = isNewObject() ? defaultValue : f.getDbl(o);
		inputDbl(x, label, f.getName(), value, styleClass);
		dbfields.add(f.getName());
	}

	final protected void inputDbl(final xwriter x, final String label, final String field, final double value,
			final String styleClass) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatDbl(value));
		x.inp(e, null, styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputElem(final xwriter x, final String field, final a elem) throws Throwable {
		final a e = fields.get(field);
		x.tr().td(2, "val");
		if (e == null) {
			elem.parent(this);
			elem.name(field);
			fields.put(field, elem);
			elem.to(x);
		} else {
			if (elem != e) {
				throw new RuntimeException("expected same element");
			}
			e.to(x);
		}
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
			if (elem != e) {
				throw new RuntimeException("expected same element");
			}
			e.to(x);
		}
	}

	final protected void inputFlt(final xwriter x, final String label, final DbObject o, final FldFlt f,
			final float defaultValue, final String styleClass) {
		final float value = isNewObject() ? defaultValue : f.getFlt(o);
		inputFlt(x, label, f.getName(), value, styleClass);
		dbfields.add(f.getName());
	}

	final protected void inputFlt(final xwriter x, final String label, final String field, final float value,
			final String styleClass) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatFlt(value));
		x.inp(e, null, styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputInt(final xwriter x, final String label, final DbObject o, final FldInt f,
			final int defaultValue, final String styleClass) {
		final int value = isNewObject() ? defaultValue : f.getInt(o);
		inputInt(x, label, f.getName(), value, styleClass);
		dbfields.add(f.getName());
	}

	final protected void inputInt(final xwriter x, final String label, final String field, final int value,
			final String styleClass) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatInt(value));
		x.inp(e, null, styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputLng(final xwriter x, final String label, final DbObject o, final FldLng f,
			final long defaultValue, final String styleClass) {
		final long value = isNewObject() ? defaultValue : f.getLng(o);
		inputLng(x, label, f.getName(), value, styleClass);
		dbfields.add(f.getName());
	}

	final protected void inputLng(final xwriter x, final String label, final String field, final long value,
			final String styleClass) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatLng(value));
		x.inp(e, null, styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected void inputRef(final xwriter x, final String label, final DbObject o, final RelRef rel,
			final int defaultValue, final Class<? extends View> selectViewClass,
			final Class<? extends Form> createFormCls) {
		inputRef(x, label, rel.getName(), o, rel, defaultValue, selectViewClass, createFormCls);
	}

	final protected void inputRef(final xwriter x, final String label, final String field, final DbObject o,
			final RelRef rel, final int defaultValue, final Class<? extends View> selectViewClass,
			final Class<? extends Form> createFormCls) {
		InputRelRef e = (InputRelRef) fields.get(field);
		if (e == null) {
			e = new InputRelRef(o, rel, defaultValue, selectViewClass, createFormCls);
			e.parent(this);
			e.name(field);
			fields.put(field, e);
		}
		x.tr().td("lbl").p(label).p(":").td("val");
		x.divh(e);
	}

	final protected void inputAgg(final xwriter x, final String label, final List<String> idPath, final DbObject o,
			final RelAgg rel, final Class<? extends Form> createFormCls) {
		inputAgg(x, label, rel.getName(), idPath, o, rel, createFormCls);
	}

	final protected void inputAgg(final xwriter x, final String label, final String field, final List<String> idPath,
			final DbObject o, final RelAgg rel, final Class<? extends Form> createFormCls) {
		InputRelAgg e = (InputRelAgg) fields.get(field);
		if (e == null) {
			e = new InputRelAgg(idPath, o, rel, createFormCls);
			e.parent(this);
			e.name(field);
			fields.put(field, e);
		}
		x.tr().td("lbl").p(label).p(":").td("val");
		x.divh(e);
	}

	final protected void inputAggN(final xwriter x, final String label, final List<String> idPath, final DbObject o,
			final RelAggN rel, final Class<? extends Form> createFormCls) {
		inputAggN(x, label, rel.getName(), idPath, o, rel, createFormCls);
	}

	final protected void inputAggN(final xwriter x, final String label, final String field, final List<String> idPath,
			final DbObject o, final RelAggN rel, final Class<? extends Form> createFormCls) {
		InputRelAggN e = (InputRelAggN) fields.get(field);
		if (e == null) {
			e = new InputRelAggN(idPath, o, rel, createFormCls);
			e.parent(this);
			e.name(field);
			fields.put(field, e);
		}
		x.tr().td("lbl").p(label).p(":").td("val");
		x.divh(e);
	}

	final protected void inputRefN(final xwriter x, final String label, final DbObject o, final RelRefN rel,
			final Set<String> defaultValues, final Class<? extends View> selectViewClass,
			final Class<? extends Form> createFormCls) {
		inputRefN(x, label, rel.getName(), o, rel, defaultValues, selectViewClass, createFormCls);
	}

	final protected void inputRefN(final xwriter x, final String label, final String field, final DbObject o,
			final RelRefN rel, final Set<String> defaultValues, final Class<? extends View> selectViewClass,
			final Class<? extends Form> createFormCls) {
		InputRelRefN e = (InputRelRefN) fields.get(field);
		if (e == null) {
			e = new InputRelRefN(o, rel, defaultValues, selectViewClass, createFormCls, "<br>");
			e.parent(this);
			e.name(field);
			fields.put(field, e);
		}
		x.tr().td("lbl").p(label).p(":").td("val");
		x.divh(e);
	}

	final protected void inputText(final xwriter x, final String label, final DbObject o, final FldStr f,
			final String defaultValue, final String styleClass) {
		final String value = isNewObject() ? defaultValue : f.getStr(o);
		inputText(x, label, f.getName(), value, styleClass);
		dbfields.add(f.getName());
	}

	final protected void inputText(final xwriter x, final String label, final String field, final String value,
			final String styleClass) {
		x.tr().td("lbl").p(label).p(":").td("val");
		x.inp(elemFor(field, value), null, styleClass, null, null, this, "sc", null, null).nl();
	}

	final protected void inputTextArea(final xwriter x, final String label, final DbObject o, final FldStr f,
			final String defaultValue, final String styleClass) {
		final String value = isNewObject() ? defaultValue : f.getStr(o);
		inputTextArea(x, label, f.getName(), value, styleClass);
		dbfields.add(f.getName());
	}

	final protected void inputTextArea(final xwriter x, final String label, final String field, final String value,
			final String styleClass) {
		x.tr().td("lbl").p(label).p(":").td("val");
		x.inptxtarea(elemFor(field, value), styleClass).nl();
	}

	final protected void inputTimestamp(final xwriter x, final String label, final DbObject o, final FldTs f,
			final Timestamp defaultValue) {
		final Timestamp value = isNewObject() ? defaultValue : f.getTs(o);
		inputTimestamp(x, label, f.getName(), value, null);
		dbfields.add(f.getName());
	}

	final protected void inputTimestamp(final xwriter x, final String label, final String field, final Timestamp value,
			final String styleClass) {
		x.tr().td("lbl").p(label).p(":").td("val");
		final a e = elemFor(field, formatDateTime(value));
		x.inp(e, "datetime-local", styleClass, null, null, this, "sc", null, null);
		x.nl();
	}

	final protected Timestamp parseDate(final String s) throws ParseException {
		if (Util.isEmpty(s)) {
			return null;
		}
		if (fmtDate == null) {
			fmtDate = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
		}
		return new Timestamp(fmtDate.parse(s).getTime());
	}

	final protected Timestamp parseDateTime(final String s) throws ParseException {
		if (Util.isEmpty(s)) {
			return null;
		}
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

	private DbField getDbFieldFromClass(final String name) {
		try {
			return (DbField) objCls.getField(name).get(null);
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	protected final void save(final xwriter x) throws Throwable {
		final DbObject o;
		if (objectId == null) {
			// create new
			o = createObject();
			objectId = Integer.toString(o.id());
		} else {
			o = getObject();
		}
		// ? ugly instanceof chain. separation of concerns ok.
		// elements that know how to write to objects
		for (final String fieldName : dbfields) {
			final DbField dbf = getDbFieldFromClass(fieldName);
			if (!o.getClass().equals(dbf.getDeclaringClass())) {
				continue;
			}
			if (dbf instanceof FldStr) {
				dbf.setObj(o, getStr(fieldName));
				continue;
			}
			if (dbf instanceof FldBool) {
				dbf.setObj(o, getBool(fieldName));
				continue;
			}
			try {
				if (dbf instanceof FldDateTime) {
					Timestamp ts;
					try {
						ts = getDateTime(fieldName);
					} catch (final ParseException ok) {
						ts = getDate(fieldName);
					}
					dbf.setObj(o, ts);
					continue;
				}
				if (dbf instanceof FldTs) {
					Timestamp ts;
					try {
						ts = getDateTime(fieldName);
					} catch (final ParseException ok) {
						ts = getDate(fieldName);
					}
					dbf.setObj(o, ts);
					continue;
				}
			} catch (final ParseException e) {
				x.xfocus(getElem(fieldName));
				throw new Exception("Time cannot be parsed.");
			}
			try {
				if (dbf instanceof FldInt) {
					dbf.setObj(o, getInt(fieldName));
					continue;
				}
				if (dbf instanceof FldLng) {
					dbf.setObj(o, getLng(fieldName));
					continue;
				}
				if (dbf instanceof FldFlt) {
					dbf.setObj(o, getFlt(fieldName));
					continue;
				}
				if (dbf instanceof FldDbl) {
					dbf.setObj(o, getDbl(fieldName));
					continue;
				}
			} catch (final ParseException e) {
				x.xfocus(getElem(fieldName));
				throw new Exception("Number cannot be parsed.");
			}
		}
		// relations
		for (final a e : fields.values()) {
			if (e instanceof InputRelRefN) {
				final InputRelRefN ir = (InputRelRefN) e;
				if (o.getClass().equals(ir.objCls)) {
					ir.save(o);
				}
				continue;
			}
			if (e instanceof InputRelRef) {
				final InputRelRef r = (InputRelRef) e;
				if (o.getClass().equals(r.objCls)) {
					r.save(o);
				}
				continue;
			}
		}
		writeToObject(x, o);
	}

	protected abstract void writeToObject(final xwriter x, final DbObject obj) throws Throwable;

	/** Focuses on field from "save" when for example validation failed. */
	final protected void xfocus(final xwriter x, final DbField f) {
		xfocus(x, f.getName());
	}

	/** Focuses on field from "save" when for example validation failed. */
	final protected void xfocus(final xwriter x, final String field) {
		x.xfocus(getElem(field));
	}

	private a elemFor(final String name, final Object value) {
		a e = fields.get(name);
		if (e == null) {
			e = new a(this, name);
			e.set(Util.toStr(value, null));
			fields.put(name, e);
		}
		return e;
	}
}
