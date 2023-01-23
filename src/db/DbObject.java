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
	public final int id() {
		return id.getId(this);
	}

	public final int getId() {
		return id.getId(this);
	}

	final void markDirty(final DbField field) {
		getCreatedDirtyFields().add(field);
		Db.currentTransaction().dirtyObjects.add(this);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName()).append("{");
		for (final Object v : fieldValues) {
			sb.append(v).append(",");
		}
		sb.setLength(sb.length() - 1);
		sb.append("}");
		return sb.toString();
	}
}
