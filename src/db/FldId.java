// reviewed: 2024-08-05
//           2025-04-28
package db;

/** Primary key integer field. */
public final class FldId extends DbField {

    FldId() {
        super("int", 0, null, null, false, false);
    }

    public void setId(final DbObject ths, final int v) {
        setObj(ths, v);
    }

    public int getId(final DbObject ths) {
        return (Integer) getObj(ths);
    }

    @Override
    protected void appendSqlColumnDefinition(final StringBuilder sb) {
        sb.append(name).append(' ').append(getSqlType()).append(" primary key auto_increment");
    }

}
