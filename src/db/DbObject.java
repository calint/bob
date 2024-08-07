// reviewed: 2024-08-05
package db;

import java.util.HashSet;

/** Abstract database object. */
public abstract class DbObject {

    public static final FldId id = new FldId();

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

    protected void onCreate() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final DbClass dbc = Db.getDbClassForJavaClass(getClass());
        sb.append(getClass().getName()).append("{");
        int i = 0;
        for (final Object v : fieldValues) {
            sb.append(dbc.allFields.get(i).getName()).append("=").append(v).append(", ");
            i++;
        }
        sb.setLength(sb.length() - ", ".length());
        sb.append("}");
        return sb.toString();
    }

}
