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

    private final Class<? extends DbObject> objCls;
    private final ArrayList<String> dbfields = new ArrayList<String>();
    private final LinkedHashMap<String, a> fields = new LinkedHashMap<String, a>();
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
        if (objectId == null && isCreateObjectAtInit()) {
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

    protected final void beginForm(final xwriter x) {
        x.table("f").nl();
    }

    protected DbObject createObject() {
        return Db.currentTransaction().create(objCls);
    }

    protected final void endForm(final xwriter x) {
        x.table_().nl();
    }

    /** Focuses on field from "render". */
    protected final void focus(final xwriter x, final DbField f) {
        focus(x, f.getName());
    }

    /** Focuses on field from "render". */
    protected final void focus(final xwriter x, final String field) {
        x.script().xfocus(getElem(field)).script_();
    }

    protected final String formatDate(final Timestamp ts) {
        if (ts == null) {
            return "";
        }
        if (fmtDate == null) {
            fmtDate = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
        }
        return fmtDate.format(ts);
    }

    protected final String formatDateTime(final Timestamp ts) {
        if (ts == null) {
            return "";
        }
        if (fmtDateTime == null) {
            fmtDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"); // format of input type datetime-local
        }
        return fmtDateTime.format(ts);
    }

    protected final String formatDbl(final double i) {
        if (fmtNbr == null) {
            fmtNbr = NumberFormat.getNumberInstance();
        }
        return fmtNbr.format(i);
    }

    protected final String formatFlt(final float i) {
        if (fmtNbr == null) {
            fmtNbr = NumberFormat.getNumberInstance();
        }
        return fmtNbr.format(i);
    }

    protected final String formatInt(final int i) {
        if (fmtNbr == null) {
            fmtNbr = NumberFormat.getNumberInstance();
        }
        return fmtNbr.format(i);
    }

    protected final String formatLng(final long i) {
        if (fmtNbr == null) {
            fmtNbr = NumberFormat.getNumberInstance();
        }
        return fmtNbr.format(i);
    }

    protected final boolean getBool(final DbField field) {
        return getBool(field.getName());
    }

    protected final boolean getBool(final String field) {
        return "1".equals(getStr(field));
    }

    protected final Timestamp getDate(final FldDateTime field) throws ParseException {
        return getDate(field.getName());
    }

    protected final Timestamp getDate(final String field) throws ParseException {
        return parseDate(getStr(field));
    }

    protected final Timestamp getDateTime(final FldDateTime field) throws ParseException {
        return getDateTime(field.getName());
    }

    protected final Timestamp getDateTime(final String field) throws ParseException {
        return parseDateTime(getStr(field));
    }

    protected final double getDbl(final FldDbl field) throws ParseException {
        return getDbl(field.getName());
    }

    protected final double getDbl(final String field) throws ParseException {
        return parseDbl(getStr(field));
    }

    protected final a getElem(final DbField f) {
        return getElem(f.getName());
    }

    protected final a getElem(final String field) {
        return fields.get(field);
    }

    protected final float getFlt(final FldFlt field) throws ParseException {
        return getFlt(field.getName());
    }

    protected final float getFlt(final String field) throws ParseException {
        return parseFlt(getStr(field));
    }

    protected final int getInt(final FldInt field) throws ParseException {
        return getInt(field.getName());
    }

    protected final int getInt(final String field) throws ParseException {
        return parseInt(getStr(field));
    }

    protected final long getLng(final FldLng field) throws ParseException {
        return getLng(field.getName());
    }

    protected final long getLng(final String field) throws ParseException {
        return parseLng(getStr(field));
    }

    protected DbObject getObject() {
        return Db.currentTransaction().get(objCls, getObjectId());
    }

    protected final int getSelectedId(final RelRef rel) {
        return getSelectedId(rel.getName());
    }

    protected final int getSelectedId(final String field) {
        final InputRelRef e = (InputRelRef) fields.get(field);
        return e.getSelectedId();
    }

    protected final Set<String> getSelectedIds(final RelRefN rel) {
        return getSelectedIds(rel.getName());
    }

    protected final Set<String> getSelectedIds(final String field) {
        final InputRelRefN e = (InputRelRefN) fields.get(field);
        return e.getSelectedIds();
    }

    protected final String getStr(final FldStr f) {
        return getStr(f.getName());
    }

    protected final String getStr(final String field) {
        final a e = fields.get(field);
        return e.str().trim();
    }

    protected final void includeView(final xwriter x, final String field, final View view) throws Throwable {
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

    protected final void inputBool(final xwriter x, final String label, final DbObject o, final FldBool f,
            final boolean defaultValue) {
        final boolean value = isNewObject() ? defaultValue : f.getBool(o);
        inputBool(x, label, f.getName(), value, null);
        dbfields.add(f.getName());
    }

    protected final void inputBool(final xwriter x, final String label, final String field, final boolean value,
            final String styleClass) {
        x.tr().td("lbl").p(label).p(":").td("val");
        final a e = elemFor(field, value ? "1" : "0");
        x.inp(e, "checkbox", styleClass, null, null, this, "sc", null, null);
        x.nl();
    }

    protected final void inputDate(final xwriter x, final String label, final DbObject o, final FldDateTime f,
            final Timestamp defaultValue) {
        final Timestamp value = isNewObject() ? defaultValue : f.getDateTime(o);
        inputDate(x, label, f.getName(), value, null);
        dbfields.add(f.getName());
    }

    protected final void inputDate(final xwriter x, final String label, final String field, final Timestamp value,
            final String styleClass) {
        x.tr().td("lbl").p(label).p(":").td("val");
        final a e = elemFor(field, formatDate(value));
        x.inp(e, "date", styleClass, null, null, this, "sc", null, null);
        x.nl();
    }

    protected final void inputDateTime(final xwriter x, final String label, final DbObject o, final FldDateTime f,
            final Timestamp defaultValue) {
        final Timestamp value = isNewObject() ? defaultValue : f.getDateTime(o);
        inputDateTime(x, label, f.getName(), value, null);
        dbfields.add(f.getName());
    }

    protected final void inputDateTime(final xwriter x, final String label, final String field, final Timestamp value,
            final String styleClass) {
        x.tr().td("lbl").p(label).p(":").td("val");
        final a e = elemFor(field, formatDateTime(value));
        x.inp(e, "datetime-local", styleClass, null, null, this, "sc", null, null);
        x.nl();
    }

    protected final void inputDbl(final xwriter x, final String label, final DbObject o, final FldDbl f,
            final double defaultValue, final String styleClass) {
        final double value = isNewObject() ? defaultValue : f.getDbl(o);
        inputDbl(x, label, f.getName(), value, styleClass);
        dbfields.add(f.getName());
    }

    protected final void inputDbl(final xwriter x, final String label, final String field, final double value,
            final String styleClass) {
        x.tr().td("lbl").p(label).p(":").td("val");
        final a e = elemFor(field, formatDbl(value));
        x.inp(e, null, styleClass, null, null, this, "sc", null, null);
        x.nl();
    }

    protected final void inputElem(final xwriter x, final String field, final a elem) throws Throwable {
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

    protected final void inputElem(final xwriter x, final String label, final String field, final a elem)
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

    protected final void inputFlt(final xwriter x, final String label, final DbObject o, final FldFlt f,
            final float defaultValue, final String styleClass) {
        final float value = isNewObject() ? defaultValue : f.getFlt(o);
        inputFlt(x, label, f.getName(), value, styleClass);
        dbfields.add(f.getName());
    }

    protected final void inputFlt(final xwriter x, final String label, final String field, final float value,
            final String styleClass) {
        x.tr().td("lbl").p(label).p(":").td("val");
        final a e = elemFor(field, formatFlt(value));
        x.inp(e, null, styleClass, null, null, this, "sc", null, null);
        x.nl();
    }

    protected final void inputInt(final xwriter x, final String label, final DbObject o, final FldInt f,
            final int defaultValue, final String styleClass) {
        final int value = isNewObject() ? defaultValue : f.getInt(o);
        inputInt(x, label, f.getName(), value, styleClass);
        dbfields.add(f.getName());
    }

    protected final void inputInt(final xwriter x, final String label, final String field, final int value,
            final String styleClass) {
        x.tr().td("lbl").p(label).p(":").td("val");
        final a e = elemFor(field, formatInt(value));
        x.inp(e, null, styleClass, null, null, this, "sc", null, null);
        x.nl();
    }

    protected final void inputLng(final xwriter x, final String label, final DbObject o, final FldLng f,
            final long defaultValue, final String styleClass) {
        final long value = isNewObject() ? defaultValue : f.getLng(o);
        inputLng(x, label, f.getName(), value, styleClass);
        dbfields.add(f.getName());
    }

    protected final void inputLng(final xwriter x, final String label, final String field, final long value,
            final String styleClass) {
        x.tr().td("lbl").p(label).p(":").td("val");
        final a e = elemFor(field, formatLng(value));
        x.inp(e, null, styleClass, null, null, this, "sc", null, null);
        x.nl();
    }

    protected final void inputRef(final xwriter x, final String label, final DbObject o, final RelRef rel,
            final int defaultValue, final Class<? extends View> selectViewClass,
            final Class<? extends Form> createFormCls) {
        inputRef(x, label, rel.getName(), o, rel, defaultValue, selectViewClass, createFormCls);
    }

    protected final void inputRef(final xwriter x, final String label, final String field, final DbObject o,
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

    protected final void inputAgg(final xwriter x, final String label, final List<String> idPath, final DbObject o,
            final RelAgg rel, final Class<? extends Form> createFormCls) {
        inputAgg(x, label, rel.getName(), idPath, o, rel, createFormCls);
    }

    protected final void inputAgg(final xwriter x, final String label, final String field, final List<String> idPath,
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

    protected final void inputAggN(final xwriter x, final String label, final List<String> idPath, final DbObject o,
            final RelAggN rel, final Class<? extends Form> createFormCls) {
        inputAggN(x, label, rel.getName(), idPath, o, rel, createFormCls);
    }

    protected final void inputAggN(final xwriter x, final String label, final String field, final List<String> idPath,
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

    protected final void inputRefN(final xwriter x, final String label, final DbObject o, final RelRefN rel,
            final Set<String> defaultValues, final Class<? extends View> selectViewClass,
            final Class<? extends Form> createFormCls) {
        inputRefN(x, label, rel.getName(), o, rel, defaultValues, selectViewClass, createFormCls);
    }

    protected final void inputRefN(final xwriter x, final String label, final String field, final DbObject o,
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

    protected final void inputText(final xwriter x, final String label, final DbObject o, final FldStr f,
            final String defaultValue, final String styleClass) {
        final String value = isNewObject() ? defaultValue : f.getStr(o);
        inputText(x, label, f.getName(), value, styleClass);
        dbfields.add(f.getName());
    }

    protected final void inputText(final xwriter x, final String label, final String field, final String value,
            final String styleClass) {
        x.tr().td("lbl").p(label).p(":").td("val");
        x.inp(elemFor(field, value), null, styleClass, null, null, this, "sc", null, null).nl();
    }

    protected final void inputTextArea(final xwriter x, final String label, final DbObject o, final FldStr f,
            final String defaultValue, final String styleClass) {
        final String value = isNewObject() ? defaultValue : f.getStr(o);
        inputTextArea(x, label, f.getName(), value, styleClass);
        dbfields.add(f.getName());
    }

    protected final void inputTextArea(final xwriter x, final String label, final String field, final String value,
            final String styleClass) {
        x.tr().td("lbl").p(label).p(":").td("val");
        x.inptxtarea(elemFor(field, value), styleClass).nl();
    }

    protected final void inputTimestamp(final xwriter x, final String label, final DbObject o, final FldTs f,
            final Timestamp defaultValue) {
        final Timestamp value = isNewObject() ? defaultValue : f.getTs(o);
        inputTimestamp(x, label, f.getName(), value, null);
        dbfields.add(f.getName());
    }

    protected final void inputTimestamp(final xwriter x, final String label, final String field, final Timestamp value,
            final String styleClass) {
        x.tr().td("lbl").p(label).p(":").td("val");
        final a e = elemFor(field, formatDateTime(value));
        x.inp(e, "datetime-local", styleClass, null, null, this, "sc", null, null);
        x.nl();
    }

    protected final Timestamp parseDate(final String s) throws ParseException {
        if (Util.isEmpty(s)) {
            return null;
        }
        if (fmtDate == null) {
            fmtDate = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
        }
        return new Timestamp(fmtDate.parse(s).getTime());
    }

    protected final Timestamp parseDateTime(final String s) throws ParseException {
        if (Util.isEmpty(s)) {
            return null;
        }
        if (fmtDateTime == null) {
            fmtDateTime = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm"); // format of input type datetime-local
        }
        return new Timestamp(fmtDateTime.parse(s).getTime());
    }

    protected final double parseDbl(final String s) throws ParseException {
        if (fmtNbr == null) {
            fmtNbr = NumberFormat.getNumberInstance();
        }
        return fmtNbr.parse(s).doubleValue();
    }

    protected final float parseFlt(final String s) throws ParseException {
        if (fmtNbr == null) {
            fmtNbr = NumberFormat.getNumberInstance();
        }
        return fmtNbr.parse(s).floatValue();
    }

    protected final int parseInt(final String s) throws ParseException {
        if (fmtNbr == null) {
            fmtNbr = NumberFormat.getNumberInstance();
        }
        return fmtNbr.parse(s).intValue();
    }

    protected final long parseLng(final String s) throws ParseException {
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

    /** Override and return true to create the object at form initiation. */
    protected boolean isCreateObjectAtInit() {
        return false;
    }

    /** Focuses on field from "save" when for example validation failed. */
    protected final void xfocus(final xwriter x, final DbField f) {
        xfocus(x, f.getName());
    }

    /** Focuses on field from "save" when for example validation failed. */
    protected final void xfocus(final xwriter x, final String field) {
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
