package db;

import java.io.Serializable;
import java.util.ArrayList;

import db.Query.TableAliasMap;

/** Parameter to get(...) sorting the result list. */
public final class Order implements Serializable {
	private static final long serialVersionUID = 1L;

	final private static class Elem implements Serializable {
		private static final long serialVersionUID = 1L;
		String tableName;
		String columnName;
		String dir;
	}

	final private ArrayList<Elem> elems = new ArrayList<Elem>();

	public Order() {
	}

	public Order(final DbField fld) {
		append(fld, true);
	}

	public Order(final DbField fld, final boolean ascending) {
		append(fld, ascending);
	}

	/** Sort on id. */
	public Order(final Class<? extends DbObject> cls, final boolean ascending) {
		final Elem e = new Elem();
		e.tableName = Db.tableNameForJavaClass(cls);
		e.columnName = DbObject.id.name;
		e.dir = ascending ? "" : "desc";
		elems.add(e);
	}

	/** Sort on id. */
	public Order(final Class<? extends DbObject> cls) {
		this(cls, true);
	}

	public Order append(final DbField fld) {
		return append(fld, true);
	}

	public Order append(final DbField fld, final boolean ascending) {
		final Elem e = new Elem();
		e.tableName = fld.tableName;
		e.columnName = fld.name;
		e.dir = ascending ? "" : "desc";
		elems.add(e);
		return this;
	}

	void sql_appendToQuery(final StringBuilder sb, final Query.TableAliasMap tam) {
		if (elems.isEmpty())
			return;
		sb.append("order by ");
		for (final Elem e : elems) {
			final String s = tam.getAliasForTableName(e.tableName);
			sb.append(s).append('.').append(e.columnName);
			if (e.dir.length() > 0) {
				sb.append(' ').append(e.dir);
			}
			sb.append(',');
		}
		sb.setLength(sb.length() - 1);
		sb.append(' ');
	}

	public Order append(final Order ord) {
		for (final Elem e : ord.elems) {
			final Elem ne = new Elem();
			ne.tableName = e.tableName;
			ne.columnName = e.columnName;
			ne.dir = e.dir;
			elems.add(ne);
		}
		return this;
	}

	@Override
	public String toString() {
		final TableAliasMap tam = new TableAliasMap();
		final StringBuilder sb = new StringBuilder(256);
		sql_appendToQuery(sb, tam);
		return sb.toString();
	}
}
