// reviewed: 2024-08-05
package db;

import java.util.ArrayList;

import db.Query.TableAliasMap;

/** Parameter to get(...) sorting the result list. */
public final class Order {

    private static final class Elem {
        String tableName;
        String columnName;
        String dir;
    }

    private final ArrayList<Elem> elems = new ArrayList<Elem>();

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

    public boolean isEmpty() {
        return elems.isEmpty();
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
        if (elems.isEmpty()) {
            return;
        }
        sb.append("order by ");
        for (final Elem e : elems) {
            final String s = tam.getAliasForTableName(e.tableName);
            sb.append(s).append('.').append(e.columnName);
            if (e.dir.length() > 0) {
                sb.append(' ').append(e.dir);
            }
            sb.append(',');
        }
        sb.setLength(sb.length() - 1); // remove last coma
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
