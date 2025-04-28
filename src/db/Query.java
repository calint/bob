//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Parameter to get(...) and getCount(...) filtering and joining on relations.
 */
public final class Query {

    // operation of query element
    public final static int EQ = 1;
    public final static int NEQ = 2;
    public final static int GT = 3;
    public final static int GTE = 4;
    public final static int LT = 5;
    public final static int LTE = 6;
    public final static int LIKE = 7;
    /** Full text query. */
    public final static int FTQ = 8;

    // operations between query elements
    public final static int NOP = 0;
    public final static int AND = 1;
    public final static int OR = 2;

    private final static class Elem {
        private Query query;// if not null then this is a sub query
        private IndexFt ftIx;// if not null then this is a full text query
        private int elemOp; // 'and', 'or' or 'nop'
        private String lhTbl;// left hand table name
        private String lh; // left hand field name
        private int op; // EQ, NEQ etc
        private String rhTbl;
        private String rh;

        private void appendElemOp(final StringBuilder sb, final int op) {
            switch (op) {
            case AND:
                sb.append(" and ");
                break;
            case OR:
                sb.append(" or ");
                break;
            case NOP:
                break;
            default:
                throw new RuntimeException("invalid elemOp " + op);
            }
        }

        public void appendSql(final StringBuilder sb, final TableAliasMap tam) {
            // sub query
            if (query != null) {
                final StringBuilder sbSubQuery = new StringBuilder(128); // ? magic number
                query.appendSql(sbSubQuery, tam);
                if (sbSubQuery.length() != 0) {
                    appendElemOp(sb, elemOp);
                    sb.append('(');
                    sb.append(sbSubQuery);
                    sb.append(")");
                }
                return;
            }

            appendElemOp(sb, elemOp);

            // full text query
            if (ftIx != null) {
                sb.append("match(");
                for (final DbField f : ftIx.fields) {
                    final String tblAlias = tam.getAliasForTableName(ftIx.tableName);
                    sb.append(tblAlias).append('.').append(f.name).append(',');
                }
                sb.setLength(sb.length() - 1); // remove trailing ','
                sb.append(')');
                sb.append(" against('");
                DbField.escapeSqlString(sb, rh);
                sb.append("' in boolean mode)");
                return;
            }

            // left hand right hand
            if (lhTbl != null) {
                sb.append(tam.getAliasForTableName(lhTbl)).append('.');
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

            if (rhTbl != null) {
                sb.append(tam.getAliasForTableName(rhTbl)).append('.');
            }
            sb.append(rh);
        }
    }

    final static class TableAliasMap {
        private int seq;
        private final HashMap<String, String> tblToAlias = new HashMap<String, String>();

        String getAliasForTableName(final String tblName) {
            String tblAlias = tblToAlias.get(tblName);
            if (tblAlias == null) {
                seq++;
                tblAlias = "t" + seq;
                tblToAlias.put(tblName, tblAlias);
            }
            return tblAlias;
        }

        void appendSqlSelectFromTables(final StringBuilder sb) {
            if (tblToAlias.isEmpty()) {
                return;
            }
            for (final Map.Entry<String, String> kv : tblToAlias.entrySet()) {
                sb.append(kv.getKey()).append(" ").append(kv.getValue()).append(",");
            }
            sb.setLength(sb.length() - 1); // remove trailing ','
        }
    }

    private final ArrayList<Elem> elems = new ArrayList<Elem>();

    private static String sqlStr(final String s) {
        final StringBuilder sb = new StringBuilder(s.length() + 10); // ? magic number
        sb.append('\'');
        DbField.escapeSqlString(sb, s);
        sb.append('\'');
        return sb.toString();
    }

    void appendSql(final StringBuilder sb, final TableAliasMap tam) {
        for (final Elem e : elems) {
            e.appendSql(sb, tam);
        }
    }

    public Query() {
    }

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

    public Query(final DbField lh, final int op, final float rh) {
        append(NOP, lh.tableName, lh.name, op, null, Float.toString(rh));
    }

    public Query(final DbField lh, final int op, final double rh) {
        append(NOP, lh.tableName, lh.name, op, null, Double.toString(rh));
    }

    public Query(final DbField lh, final int op, final boolean rh) {
        append(NOP, lh.tableName, lh.name, op, null, Boolean.toString(rh));
    }

    public Query(final DbField lh, final int op, final Timestamp ts) {
        append(NOP, lh.tableName, lh.name, op, null, "'" + ts.toString() + "'");
    }

    public Query(final DbField lh, final int op, final DbField rh) {
        append(NOP, lh.tableName, lh.name, op, rh.tableName, rh.name);
    }

    /** Join. */
    public Query(final RelAggN rel) {
        append(NOP, rel.tableName, DbObject.id.name, EQ, rel.toTableName, rel.relFld.name);
    }

    /** Join. */
    public Query(final RelRefN rel) {
        append(NOP, rel.tableName, DbObject.id.name, EQ, rel.rrm.tableName, RelRefNMeta.COLUMN_NAME_FROM).append(AND,
                rel.rrm.tableName, RelRefNMeta.COLUMN_NAME_TO, EQ, rel.rrm.toTableName, DbObject.id.name);
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
    public Query(final IndexFt ix, final String ftQuery) {
        final Elem e = new Elem();
        e.elemOp = NOP;
        e.ftIx = ix;
        e.rh = ftQuery;
        elems.add(e);
    }

    public Query and(final Query q) {
        return append(AND, q);
    }

    public Query or(final Query q) {
        return append(OR, q);
    }

    private Query append(final int elemOp, final String lhTbl, final String lh, final int op, final String rhTbl,
            final String rh) {
        final Elem e = new Elem();
        if (elems.isEmpty()) {
            // first elem is always NOP
            e.elemOp = NOP;
        } else {
            e.elemOp = elemOp;
        }
        e.lhTbl = lhTbl;
        e.lh = lh;
        e.op = op;
        e.rhTbl = rhTbl;
        e.rh = rh;
        elems.add(e);
        return this;
    }

    private Query append(final int elemOp, final Query q) {
        final Elem e = new Elem();
        if (elems.isEmpty()) {
            // first elem is always NOP
            e.elemOp = NOP;
        } else {
            e.elemOp = elemOp;
        }
        e.query = q;
        elems.add(e);
        return this;
    }

    public Query and(final Class<? extends DbObject> c, final int id) {
        return append(AND, Db.tableNameForJavaClass(c), DbObject.id.name, EQ, null, Integer.toString(id));
    }

    public Query and(final DbField lh, final int op, final String rh) {
        return append(AND, lh.tableName, lh.name, op, null, sqlStr(rh));
    }

    public Query and(final DbField lh, final int op, final int rh) {
        return append(AND, lh.tableName, lh.name, op, null, Integer.toString(rh));
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

    public Query and(final DbField lh, final int op, final Timestamp ts) {
        return append(AND, lh.tableName, lh.name, op, null, "'" + ts.toString() + "'");
    }

    public Query and(final DbField lh, final int op, final DbField rh) {
        return append(AND, lh.tableName, lh.name, op, rh.tableName, rh.name);
    }

    public Query and(final RelAggN rel) {
        return append(AND, rel.tableName, DbObject.id.name, EQ, rel.toTableName, rel.relFld.name);
    }

    public Query and(final RelRefN rel) {
        return append(AND, rel.tableName, DbObject.id.name, EQ, rel.rrm.tableName, RelRefNMeta.COLUMN_NAME_FROM)
                .append(AND, rel.rrm.tableName, RelRefNMeta.COLUMN_NAME_TO, EQ, rel.rrm.toTableName, DbObject.id.name);
    }

    public Query and(final RelAgg rel) {
        return append(AND, rel.tableName, rel.relFld.name, EQ, rel.toTableName, DbObject.id.name);
    }

    public Query and(final RelRef rel) {
        return append(AND, rel.tableName, rel.relFld.name, EQ, rel.toTableName, DbObject.id.name);
    }

    public Query and(final IndexFt ix, final String ftQuery) {
        final Elem e = new Elem();
        if (elems.isEmpty()) {
            // first elem is always NOP
            e.elemOp = NOP;
        } else {
            e.elemOp = AND;
        }
        e.ftIx = ix;
        e.rh = ftQuery;
        elems.add(e);
        return this;
    }

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
        return append(OR, rel.tableName, DbObject.id.name, EQ, rel.rrm.tableName, RelRefNMeta.COLUMN_NAME_FROM)
                .append(AND, rel.rrm.tableName, RelRefNMeta.COLUMN_NAME_TO, EQ, rel.rrm.toTableName, DbObject.id.name);
    }

    public Query or(final RelAgg rel) {
        return append(OR, rel.tableName, rel.relFld.name, EQ, rel.toTableName, DbObject.id.name);
    }

    public Query or(final RelRef rel) {
        return append(OR, rel.tableName, rel.relFld.name, EQ, rel.toTableName, DbObject.id.name);
    }

    public Query or(final IndexFt ix, final String ftQuery) {
        final Elem e = new Elem();
        if (elems.isEmpty()) {
            // first elem is always NOP
            e.elemOp = NOP;
        } else {
            e.elemOp = OR;
        }
        e.ftIx = ix;
        e.rh = ftQuery;
        elems.add(e);
        return this;
    }

    @Override
    public String toString() {
        final TableAliasMap tam = new TableAliasMap();
        final StringBuilder sbWhere = new StringBuilder(256); // ? magic number
        appendSql(sbWhere, tam);

        final StringBuilder sbFrom = new StringBuilder(256); // ? magic number
        tam.appendSqlSelectFromTables(sbFrom);
        if (sbWhere.length() != 0) {
            sbFrom.append(" where ");
            sbFrom.append(sbWhere);
        }
        return sbFrom.toString();
    }

}
