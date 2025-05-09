//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/** Abstract database mapped field. */
public abstract class DbField {

    /** The Java class field name where this field was created. */
    protected String name;

    /** The Java class where this field was created. */
    protected Class<? extends DbObject> cls;

    /** Database table name for this field. */
    protected String tableName;

    /**
     * The SQL type as returned by TYPE_NAME.
     * {@link DatabaseMetaData}.getColumns(...).
     */
    protected final String type;

    /** The size of the SQL column where applicable (varchar, char) otherwise 0. */
    protected final int size;

    /**
     * Default value as returned by COLUMN_DEF from
     * {@link DatabaseMetaData}.getColumns(...).
     */
    protected final String sqlDefVal;

    /** Default value of type returned by {@link ResultSet}. */
    protected final Object defVal;

    /** True if field null allowed. */
    protected final boolean allowsNull;

    /**
     * True if default value is to be enclosed by quotes and escaped at column
     * definition and update.
     */
    protected final boolean isStringType;

    /** Index in the data array of object. */
    protected int slotNbr;

    /**
     * @param sqlType      The SQL type as returned by TYPE_NAME
     *                     {@link DatabaseMetaData}.getColumns(...).
     * @param size         Where applicable (varchar, char) otherwise 0.
     * @param sqlDefVal    Default value as returned by COLUMN_DEF from
     *                     {@link DatabaseMetaData}.getColumns(...).
     * @param allowsNull   True if null is allowed.
     * @param isStringType True if default value is to be enclosed by quotes and
     *                     escaped at column definition and update.
     */
    protected DbField(final String sqlType, final int size, final String sqlDefVal, final Object defVal,
            final boolean allowsNull, final boolean isStringType) {
        type = sqlType;
        this.size = size;
        this.sqlDefVal = sqlDefVal;
        this.defVal = defVal;
        this.allowsNull = allowsNull;
        this.isStringType = isStringType;
    }

    public final Class<? extends DbObject> getDeclaringClass() {
        return cls;
    }

    /**
     * Called by {@link DbClass} when asserting that column type matches
     * {@link DbField} type.
     */
    public final String getSqlType() {
        return type;
    }

    /** @return Size of field if applicable (varchar and char) or 0. */
    public final int getSize() {
        return size;
    }

    /**
     * @return The field name of the Java class where the field was created.
     */
    public final String getName() {
        return name;
    }

    /**
     * Default value as returned by COLUMN_DEF from
     * {@link DatabaseMetaData}.getColumns(...).
     */
    public final String getSqlDefaultValue() {
        return sqlDefVal;
    }

    /**
     * Called by {@link DbTransaction} at object creation.
     *
     * @return Default value as an object of type returned by {@link ResultSet}
     *         getObject(int).
     */
    public final Object getDefaultValue() {
        return defVal;
    }

    public final boolean isAllowsNull() {
        return allowsNull;
    }

    /**
     * @return True if default value is a string and should be quoted and escaped
     *         when defining column and updates.
     */
    public final boolean isDefaultValueString() {
        return isStringType;
    }

    /**
     * Append to SQL statement the definition of the column. Called by
     * {@link DbClass} at column creation and move column.
     */
    protected void appendSqlColumnDefinition(final StringBuilder sb) {
        sb.append(name).append(' ').append(getSqlType());
        if (size != 0) {
            sb.append("(").append(getSize()).append(")");
        }
        if (sqlDefVal != null) {
            sb.append(" default ");
            if (isDefaultValueString()) {
                sb.append('\'');
                escapeSqlString(sb, sqlDefVal);
                sb.append('\'');
            } else {
                sb.append(sqlDefVal);
            }
        }
        if (!allowsNull) {
            sb.append(" not null");
        }
    }

    /**
     * Append to SQL statement the value of the field. Called by
     * {@link DbTransaction} at update database from object.
     */
    protected void appendSqlUpdateValue(final StringBuilder sb, final DbObject o) {
        final Object v = getObj(o);
        if (v == null) {
            sb.append("null");
            return;
        }

        if (isStringType) {
            sb.append('\'');
            final String s = v.toString();
            sb.ensureCapacity(sb.length() + s.length() + 128); // ? magic number
            escapeSqlString(sb, s);
            sb.append('\'');
            return;
        }

        sb.append(v);
    }

    /** Sets value in object field values and marks field and object dirty. */
    public final void setObj(final DbObject ths, final Object v) {
        ths.fieldValues[slotNbr] = v;
        ths.markDirty(this);
    }

    /**
     * Sets value in object field values without marking field or object dirty. Used
     * for optimizing handling of serialized objects.
     */
    public final void putObj(final DbObject ths, final Object v) {
        ths.fieldValues[slotNbr] = v;
    }

    /** Gets value from object field values. */
    public final Object getObj(final DbObject ths) {
        return ths.fieldValues[slotNbr];
    }

    @Override
    public String toString() {
        return name;
    }

    /** Append to StringBuilder escaped string. */
    public static void escapeSqlString(final StringBuilder sb, final String s) {
        // note: from
        // https://stackoverflow.com/questions/1812891/java-escape-string-to-prevent-sql-injection
        final int len = s.length();
        for (int i = 0; i < len; ++i) {
            final char ch = s.charAt(i);
            switch (ch) {
            case 0: // Must be escaped for mysql
                sb.append("\\0");
                break;
            case '\n': // Must be escaped for logs
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '\'':
                sb.append("\\'");
                break;
            case '\032': // This gives problems on Win32
                sb.append("\\Z");
                break;
            case '\u00a5':
            case '\u20a9':
                // escape characters interpreted as backslash by mysql
                // fall through
            default:
                sb.append(ch);
            }
        }
    }

}
