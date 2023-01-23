package db;

import java.util.HashSet;

/** Abstract database object. */
public abstract class DbObject {
	public final static FldId id = new FldId();

	Object[] fieldValues;
	HashSet<DbField> dirtyFields;

	private HashSet<DbField> getCreatedDirtyFields() {
		if (dirtyFields == null) {
			dirtyFields = new HashSet<DbField>();
		}
		return dirtyFields;
	}

	/** Alias for getId(). */
	public int id() {
		return id.getId(this);
	}

	public int getId() {
		return id.getId(this);
	}

//	final protected String getStr(final DbField field) {
//		return (String) fieldValues[field.slotNbr];
//	}
//
//	final protected int getInt(final DbField field) {
//		return (Integer) fieldValues[field.slotNbr];
//	}
//
//	final protected long getLng(final DbField field) {
//		return (Long) fieldValues[field.slotNbr];
//	}
//
//	final protected float getFlt(final DbField field) {
//		return (Float) fieldValues[field.slotNbr];
//	}
//
//	final protected double getDbl(final DbField field) {
//		return (Double) fieldValues[field.slotNbr];
//	}
//
//	final protected boolean getBool(final DbField field) {
//		return (Boolean) fieldValues[field.slotNbr];
//	}
//
//	final protected Timestamp getTs(final DbField field) {
//		return (Timestamp) fieldValues[field.slotNbr];
//	}
//
//	final protected byte[] getBytesArray(final DbField field) {
//		return (byte[]) fieldValues[field.slotNbr];
//	}
//
//	final protected Object get(final DbField field) {
//		return fieldValues[field.slotNbr];
//	}
//
//	final protected void set(final DbField field, final Object value) {
//		fieldValues[field.slotNbr] = value;
//		getCreatedDirtyFields().add(field);
//		Db.currentTransaction().dirtyObjects.add(this);
//	}
//
//	/**
//	 * Puts object in the field value map without marking field dirty and triggering
//	 * an update. Used by user defined DbFields to optimize get/set data
//	 * transformations.
//	 */
//	final protected void put(final DbField field, final Object value) {
//		fieldValues[field.slotNbr] = value;
//	}
//
//	final protected void set(final DbField field, final String value) {
//		fieldValues[field.slotNbr] = value;
//		getCreatedDirtyFields().add(field);
//		Db.currentTransaction().dirtyObjects.add(this);
//	}
//
//	final protected void set(final DbField field, final int value) {
//		fieldValues[field.slotNbr] = value;
//		getCreatedDirtyFields().add(field);
//		Db.currentTransaction().dirtyObjects.add(this);
//	}
//
//	final protected void set(final DbField field, final long value) {
//		fieldValues[field.slotNbr] = value;
//		getCreatedDirtyFields().add(field);
//		Db.currentTransaction().dirtyObjects.add(this);
//	}
//
//	final protected void set(final DbField field, final Timestamp value) {
//		fieldValues[field.slotNbr] = value;
//		getCreatedDirtyFields().add(field);
//		Db.currentTransaction().dirtyObjects.add(this);
//	}
//
//	final protected void set(final DbField field, final byte[] value) {
//		fieldValues[field.slotNbr] = value;
//		getCreatedDirtyFields().add(field);
//		Db.currentTransaction().dirtyObjects.add(this);
//	}
//
//	final protected void set(final DbField field, final float value) {
//		fieldValues[field.slotNbr] = value;
//		getCreatedDirtyFields().add(field);
//		Db.currentTransaction().dirtyObjects.add(this);
//	}
//
//	final protected void set(final DbField field, final double value) {
//		fieldValues[field.slotNbr] = value;
//		getCreatedDirtyFields().add(field);
//		Db.currentTransaction().dirtyObjects.add(this);
//	}
//
//	final protected void set(final DbField field, final boolean value) {
//		fieldValues[field.slotNbr] = value;
//		getCreatedDirtyFields().add(field);
//		Db.currentTransaction().dirtyObjects.add(this);
//	}

	final protected void markDirty(final DbField field) {
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
	public static void putFieldValue(final DbObject o, final DbField f, final Object v) {
		f.putObj(o, v);
	}

	/** Sets the value v in DbField f in DbObject o. */
	public static void setFieldValue(final DbObject o, final DbField f, final Object v) {
		f.setObj(o, v);
	}

	/** @return the object for field f in DbObject o. */
	public static Object getFieldValue(final DbObject o, final DbField f) {
		return f.getObj(o);
	}
}
