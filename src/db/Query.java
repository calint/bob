package db;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Parameter to get(...) and getCount(...) for filtering and joining on
 * relations.
 */
public final class Query {
	public final static int EQ = 1;
	public final static int NEQ = 2;
	public final static int GT = 3;
	public final static int GTE = 4;
	public final static int LT = 5;
	public final static int LTE = 6;
	public final static int LIKE = 7;
	/** Full text query. */
	public final static int FTQ = 8;

	final private static class Elem {
		int elemOp;
		String lhtbl;// left hand table name
		String lh; // left hand field name
		int op;
		String rhtbl;
		String rh;
		Query query;// if not null then this is a sub query
		IndexFt ftix;// if not null then this is a full text query

		public void sql_build(final StringBuilder sb, TableAliasMap tam) {
			switch (elemOp) {
			case AND:
				sb.append("and ");
				break;
			case OR:
				sb.append("or ");
				break;
			case NOP:
				break;
			default:
				throw new RuntimeException("invalid elemOp " + elemOp);
			}

			// sub query
			if (query != null) {
				sb.append('(');
				query.sql_build(sb, tam);
				sb.append(") ");
				return;
			}

			// full text query
			if (ftix != null) {
				sb.append("match(");
				for (final DbField f : ftix.fields) {
					final String tblalias = tam.getAliasForTableName(ftix.tableName);
					sb.append(tblalias).append('.').append(f.name).append(',');
				}
				sb.setLength(sb.length() - 1);
				sb.append(')');
				sb.append(" against('");
				DbField.escapeSqlString(sb, rh);
				sb.append("' in boolean mode) ");
				return;
			}

			// left hand right hand
			if (lhtbl != null) {
				sb.append(tam.getAliasForTableName(lhtbl)).append('.');
			}
			sb.append(lh);

			switch (op) {
			case EQ:
				sb.append('=');
				break;
			case NEQ:
				sb.append("!=");
				break;
			case GT:
				sb.append('>');
				break;
			case GTE:
				sb.append(">=");
				break;
			case LT:
				sb.append('<');
				break;
			case LTE:
				sb.append("<=");
				break;
			case LIKE:
				sb.append(" like ");
				break;
			case FTQ:
				sb.append(' ');
				break;
			default:
				throw new RuntimeException("op " + op + " not supported");
			}

			if (rhtbl != null) {
				sb.append(tam.getAliasForTableName(rhtbl)).append('.');
			}
			sb.append(rh).append(' ');
		}
	}

	final static class TableAliasMap {
		private int seq;
		final private HashMap<String, String> tblToAlias = new HashMap<String, String>();

		String getAliasForTableName(final String tblname) {
			String tblalias = tblToAlias.get(tblname);
			if (tblalias == null) {
				seq++;
				tblalias = "t" + seq;
				tblToAlias.put(tblname, tblalias);
			}
			return tblalias;
		}

		void sql_appendSelectFromTables(final StringBuilder sb) {
			for (final Map.Entry<String, String> kv : tblToAlias.entrySet()) {
				sb.append(kv.getKey()).append(" ").append(kv.getValue()).append(", ");
			}
			if (!tblToAlias.isEmpty())
				sb.setLength(sb.length() - 2);
		}
	}

	private final ArrayList<Elem> elems = new ArrayList<Elem>();

	public final static int NOP = 0;
	public final static int AND = 1;
	public final static int OR = 2;

	private static String sqlStr(final String s) {
		final StringBuilder sb = new StringBuilder(s.length() + 10); // ? magic number
		sb.append('\'');
		DbField.escapeSqlString(sb, s);
		sb.append('\'');
		return sb.toString();
	}

	void sql_build(final StringBuilder sb, final TableAliasMap tam) {
		for (final Elem e : elems)
			e.sql_build(sb, tam);
	}

	public Query() {
	}

	////////////////////////////////////////////////////////
	/** Query by id. */
	public Query(final Class<? extends DbObject> c, final int id) {
		append(NOP, Db.tableNameForJavaClass(c), DbObject.id.name, EQ, null, Integer.toString(id));
	}

	public Query(final DbField lh, final int op, final String rh) {
		append(NOP, lh.tableName, lh.name, op, null, sqlStr(rh));
	}

	public Query(final DbField lh, final int op, final int rh) {
		append(NOP, lh.tableName, lh.name, op, null, Integer.toString(rh));
	}

	public Query(final DbField lh, final int op, final Timestamp ts) {
//		append(NOP, lh.tableName, lh.columnName, op, null, ts.toString());
		append(NOP, lh.tableName, lh.name, op, null, "'" + ts.toString() + "'");
	}

	public Query(final DbField lh, final int op, final DbField rh) {
		append(NOP, lh.tableName, lh.name, op, rh.tableName, rh.name);
	}

	public Query(final DbField lh, final int op, final float rh) {
		append(NOP, lh.tableName, lh.name, op, null, Float.toString(rh));
	}

	public Query(final DbField lh, final int op, final double rh) {
		append(NOP, lh.tableName, lh.name, op, null, Double.toString(rh));
	}

	public Query(final DbField lh, final int op, final boolean rh) {
		append(NOP, lh.tableName, lh.name, op, null, Boolean.toString(rh));
	}
	/** Join. */
	public Query(final RelAggN rel) {
		append(NOP, rel.tableName, DbObject.id.name, EQ, rel.toTableName, rel.relFld.name);
	}
	/** Join. */
	public Query(final RelRefN rel) {
		append(NOP, rel.tableName, DbObject.id.name, EQ, rel.rrm.tableName, rel.rrm.fromColName).append(AND,
				rel.rrm.tableName, rel.rrm.toColName, EQ, rel.rrm.toTableName, DbObject.id.name);
	}
	/** Join. */
	public Query(final RelAgg rel) {
		append(NOP, rel.tableName, rel.relFld.name, EQ, rel.toTableName, DbObject.id.name);
	}
	/** Join. */
	public Query(final RelRef rel) {
		append(NOP, rel.tableName, rel.relFld.name, EQ, rel.toTableName, DbObject.id.name);
	}
	/** Full text query. */
	public Query(final IndexFt ix, final String ftquery) {
		final Elem e = new Elem();
		e.elemOp = NOP;
		e.ftix = ix;
		e.rh = ftquery;
		elems.add(e);
	}

	///////////////////////////////////////////////////////////////////////
	public Query and(final Query q) {
		return append(AND, q);
	}

	public Query or(final Query q) {
		return append(OR, q);
	}

	private Query append(final int elemOp, final String lhtbl, final String lh, final int op, final String rhtbl,
			final String rh) {
		final Elem e = new Elem();
		e.elemOp = elemOp;
		e.lhtbl = lhtbl;
		e.lh = lh;
		e.op = op;
		e.rhtbl = rhtbl;
		e.rh = rh;
		elems.add(e);
		return this;
	}

	private Query append(final int elemOp, final Query q) {
		final Elem e = new Elem();
		e.elemOp = elemOp;
		e.query = q;
		elems.add(e);
		return this;
	}

	// - - - - - - -- --- - - -- - - -- - - -- --
	public Query and(final Class<? extends DbObject> c, final int id) {
		return append(AND, Db.tableNameForJavaClass(c), DbObject.id.name, EQ, null, Integer.toString(id));
	}

	public Query and(final DbField lh, final int op, final String rh) {
		return append(AND, lh.tableName, lh.name, op, null, sqlStr(rh));
	}

	public Query and(final DbField lh, final int op, final int rh) {
		return append(AND, lh.tableName, lh.name, op, null, Integer.toString(rh));
	}

	public Query and(final DbField lh, final int op, final Timestamp ts) {
		return append(AND, lh.tableName, lh.name, op, null, "'" + ts.toString() + "'");
	}

	public Query and(final DbField lh, final int op, final float rh) {
		return append(AND, lh.tableName, lh.name, op, null, Float.toString(rh));
	}

	public Query and(final DbField lh, final int op, final double rh) {
		return append(AND, lh.tableName, lh.name, op, null, Double.toString(rh));
	}

	public Query and(final DbField lh, final int op, final boolean rh) {
		return append(AND, lh.tableName, lh.name, op, null, Boolean.toString(rh));
	}

	public Query and(final DbField lh, final int op, final DbField rh) {
		return append(AND, lh.tableName, lh.name, op, rh.tableName, rh.name);
	}

	public Query and(final RelAggN rel) {
		return append(AND, rel.tableName, DbObject.id.name, EQ, rel.toTableName, rel.relFld.name);
	}

	public Query and(final RelRefN rel) {
		return append(AND, rel.tableName, DbObject.id.name, EQ, rel.rrm.tableName, rel.rrm.fromColName).append(AND,
				rel.rrm.tableName, rel.rrm.toColName, EQ, rel.rrm.toTableName, DbObject.id.name);
	}

	public Query and(final RelAgg rel) {
		return append(AND, rel.tableName, rel.relFld.name, EQ, rel.toTableName, DbObject.id.name);
	}

	public Query and(final RelRef rel) {
		return append(AND, rel.tableName, rel.relFld.name, EQ, rel.toTableName, DbObject.id.name);
	}

	public Query and(final IndexFt ix, final String ftquery) {
		final Elem e = new Elem();
		e.elemOp = AND;
		e.ftix = ix;
		e.rh = ftquery;
		elems.add(e);
		return this;
	}

	// - - - - - - -- --- - - -- - - -- - - -- --
	public Query or(final Class<? extends DbObject> c, final int id) {
		return append(OR, Db.tableNameForJavaClass(c), DbObject.id.name, EQ, null, Integer.toString(id));
	}

	public Query or(final DbField lh, final int op, final String rh) {
		return append(OR, lh.tableName, lh.name, op, null, sqlStr(rh));
	}

	public Query or(final DbField lh, final int op, final int rh) {
		return append(OR, lh.tableName, lh.name, op, null, Integer.toString(rh));
	}

	public Query or(final DbField lh, final int op, final float rh) {
		return append(OR, lh.tableName, lh.name, op, null, Float.toString(rh));
	}

	public Query or(final DbField lh, final int op, final double rh) {
		return append(OR, lh.tableName, lh.name, op, null, Double.toString(rh));
	}

	public Query or(final DbField lh, final int op, final boolean rh) {
		return append(OR, lh.tableName, lh.name, op, null, Boolean.toString(rh));
	}

	public Query or(final DbField lh, final int op, final Timestamp ts) {
		return append(OR, lh.tableName, lh.name, op, null, "'" + ts.toString() + "'");
	}

	public Query or(final DbField lh, final int op, final DbField rh) {
		return append(OR, lh.tableName, lh.name, op, rh.tableName, rh.name);
	}

	public Query or(final RelAggN rel) {
		return append(OR, rel.tableName, DbObject.id.name, EQ, rel.toTableName, rel.relFld.name);
	}

	public Query or(final RelRefN rel) {
		return append(OR, rel.tableName, DbObject.id.name, EQ, rel.rrm.tableName, rel.rrm.fromColName).append(AND,
				rel.rrm.tableName, rel.rrm.toColName, EQ, rel.rrm.toTableName, DbObject.id.name);
	}

	public Query or(final RelAgg rel) {
		return append(OR, rel.tableName, rel.relFld.name, EQ, rel.toTableName, DbObject.id.name);
	}

	public Query or(final RelRef rel) {
		return append(OR, rel.tableName, rel.relFld.name, EQ, rel.toTableName, DbObject.id.name);
	}

	public Query or(final IndexFt ix, final String ftquery) {
		final Elem e = new Elem();
		e.elemOp = OR;
		e.ftix = ix;
		e.rh = ftquery;
		elems.add(e);
		return this;
	}

	@Override
	public String toString() {
		final TableAliasMap tam = new TableAliasMap();
		final StringBuilder sbw = new StringBuilder(256);
		sql_build(sbw, tam);

		final StringBuilder sbf = new StringBuilder(256);
		tam.sql_appendSelectFromTables(sbf);
		sbf.append(" where ");
		sbf.append(sbw);
		return sbf.toString();
	}
}
