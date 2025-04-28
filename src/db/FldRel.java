// reviewed: 2024-08-05
//           2025-04-28
package db;

/** Field/column that refers to an id. It may be null or 0. */
final class FldRel extends DbField {

    public FldRel() {
        super("int", 0, null, null, true, false);
    }

    @Override
    protected void appendSqlUpdateValue(final StringBuilder sb, final DbObject o) {
        final int id = getId(o); // ? can this be null?
        if (id == 0) {
            sb.append("null");
            return;
        }
        sb.append(id);
    }

    public void setId(final DbObject ths, final int v) {
        setObj(ths, v);
    }

    public int getId(final DbObject ths) {
        return (Integer) getObj(ths);
    }

}
