package db;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;

/** Abstract database object. */
public abstract class DbObject {
	public final static FldId id = new FldId();

	final HashMap<DbField, Object> fieldValues = new HashMap<DbField, Object>();
	HashSet<DbField> dirtyFields;

	private HashSet<DbField> getCreatedDirtyFields() {
		if (dirtyFields == null)
			dirtyFields = new HashSet<DbField>();
		return dirtyFields;
	}

	/** Alias for getId(). */
	final public int id() {
		return ((Integer) fieldValues.get(id)).intValue();
	}

	final public int getId() {
		return id();
	}

	final protected String getStr(final DbField field) {
		return (String) fieldValues.get(field);
	}

	final protected int getInt(final DbField field) {
		return ((Integer) fieldValues.get(field)).intValue();
	}

	final protected long getLng(final DbField field) {
		return ((Long) fieldValues.get(field)).longValue();
	}

	final protected float getFlt(final DbField field) {
		return ((Float) fieldValues.get(field)).floatValue();
	}

	final protected double getDbl(final DbField field) {
		return ((Double) fieldValues.get(field)).doubleValue();
	}

	final protected boolean getBool(final DbField field) {
		return ((Boolean) fieldValues.get(field)).booleanValue();
	}

	final protected Timestamp getTs(final DbField field) {
		return (Timestamp) fieldValues.get(field);
	}

	final protected byte[] getBytesArray(final DbField field) {
		return (byte[]) fieldValues.get(field);
	}

	final protected Object get(final DbField field) {
		return fieldValues.get(field);
	}

	final protected void set(final DbField field, final Object value) {
		fieldValues.put(field, value);
		getCreatedDirtyFields().add(field);
		Db.currentTransaction().dirtyObjects.add(this);
	}

	/**
	 * Puts object in the field value map without marking field dirty triggering an
	 * update. Used by user defined DbFields to optimize get/set data
	 * transformations.
	 */
	final protected void put(final DbField field, final Object value) {
		fieldValues.put(field, value);
	}

	final protected void set(final DbField field, final String value) {
		fieldValues.put(field, value);
		getCreatedDirtyFields().add(field);
		Db.currentTransaction().dirtyObjects.add(this);
	}

	final protected void set(final DbField field, final int value) {
		fieldValues.put(field, value);
		getCreatedDirtyFields().add(field);
		Db.currentTransaction().dirtyObjects.add(this);
	}

	final protected void set(final DbField field, final long value) {
		fieldValues.put(field, value);
		getCreatedDirtyFields().add(field);
		Db.currentTransaction().dirtyObjects.add(this);
	}

	final protected void set(final DbField field, final Timestamp value) {
		fieldValues.put(field, value);
		getCreatedDirtyFields().add(field);
		Db.currentTransaction().dirtyObjects.add(this);
	}

	final protected void set(final DbField field, final byte[] value) {
		fieldValues.put(field, value);
		getCreatedDirtyFields().add(field);
		Db.currentTransaction().dirtyObjects.add(this);
	}

	final protected void set(final DbField field, final float value) {
		fieldValues.put(field, value);
		getCreatedDirtyFields().add(field);
		Db.currentTransaction().dirtyObjects.add(this);
	}

	final protected void set(final DbField field, final double value) {
		fieldValues.put(field, value);
		getCreatedDirtyFields().add(field);
		Db.currentTransaction().dirtyObjects.add(this);
	}

	final protected void set(final DbField field, final boolean value) {
		fieldValues.put(field, value);
		getCreatedDirtyFields().add(field);
		Db.currentTransaction().dirtyObjects.add(this);
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getName()).append(" ").append(fieldValues.toString()).toString();
	}

	/**
	 * Puts value v in DbField f in DbObject o. The field is not marked dirty thus
	 * update will not be triggered. Used for optimizing handling of transformed
	 * data.
	 */
	public static void putFieldValue(final DbObject o, final DbField f, Object v) {
		o.put(f, v);
	}

	/** Sets the value v in DbField f in DbObject o. */
	public static void setFieldValue(final DbObject o, final DbField f, Object v) {
		o.set(f, v);
	}

	/** @return the object for field f in DbObject o. */
	public static Object getFieldValue(final DbObject o, final DbField f) {
		return o.get(f);
	}
}
