package db;

import java.sql.DatabaseMetaData;
import java.util.Map;

/** Abstract field. */
public abstract class DbField {
	String name;
	Class<? extends DbObject> cls;
	String tableName;
	final String type;
	final int size;
	final String defVal;
	final boolean allowsNull;
	final boolean isStringType;

	/**
	 * @param sqlType      the SQL type as returned by TYPE_NAME
	 *                     {@link DatabaseMetaData}.getColumns(...).
	 * @param size         where applicable (varchar, char) otherwise 0.
	 * @param defVal       default value as returned by COLUMN_DEF from
	 *                     {@link DatabaseMetaData}.getColumns(...).
	 * @param allowsNull   true if null is allowed.
	 * @param isStringType true if default value is to be enclosed by quotes and
	 *                     escaped at column definition and update.
	 */
	protected DbField(final String sqlType, final int size, final String sqlDefVal, final boolean allowsNull,
			final boolean isStringType) {
		this.type = sqlType;
		this.size = size;
		this.defVal = sqlDefVal;
		this.allowsNull = allowsNull;
		this.isStringType = isStringType;
	}

	/**
	 * Called by {@link DbClass} when asserting that column type matches
	 * {@link DbField} type.
	 */
	public final String getSqlType() {
		return type;
	}

	/** @return size of field if applicable (varchar and char) or 0. */
	public final int getSize() {
		return size;
	}

	/**
	 * @return the field name of the Java class where the field was created.
	 */
	public final String getName() {
		return name;
	}

	public final String getDefaultValue() {
		return defVal;
	}

	public final boolean isAllowsNull() {
		return allowsNull;
	}

	/**
	 * @return true if default value is a string and should be quoted and escaped
	 *         when defining column and updates.
	 */
	public final boolean isDefaultValueString() {
		return isStringType;
	}

	/**
	 * Append to SQL statement the definition of the column. Called by
	 * {@link DbClass} at column creation and move column.
	 */
	protected void sql_columnDefinition(final StringBuilder sb) {
		sb.append(name).append(' ').append(getSqlType());
		if (size != 0)
			sb.append("(").append(getSize()).append(")");
		if (defVal != null) {
			sb.append(" default ");
			if (isDefaultValueString()) {
				sb.append('\'');
				escapeSqlString(sb, defVal);
				sb.append('\'');
			} else {
				sb.append(defVal);
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
	protected void sql_updateValue(final StringBuilder sb, final DbObject o) {
		final Object v = o.get(this);
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

	/**
	 * The field puts default value in the map. Called by {@link DbTransaction} at
	 * object creation.
	 */
	protected void putDefaultValue(final Map<DbField, Object> kvm) {
	}

	@Override
	public final int hashCode() {
		return name.hashCode();
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
			case 0: // Must be escaped for 'mysql'
				sb.append('\\');
				sb.append('0');
				break;
			case '\n': // Must be escaped for logs
				sb.append('\\');
				sb.append('n');
				break;
			case '\r':
				sb.append('\\');
				sb.append('r');
				break;
			case '\\':
				sb.append('\\');
				sb.append('\\');
				break;
			case '\'':
				sb.append('\\');
				sb.append('\'');
				break;
			case '\032': // This gives problems on Win32
				sb.append('\\');
				sb.append('Z');
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
