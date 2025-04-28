//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

/** Boolean field. */
public final class FldBool extends DbField {

    public FldBool(final boolean defVal) {
        super("bit", 1, defVal ? "b'1'" : "b'0'", defVal, false, false);
    }

    public FldBool() {
        this(false);
    }

    @Override
    protected void appendSqlUpdateValue(final StringBuilder sb, final DbObject o) {
        sb.append(getBool(o) ? "b'1'" : "b'0'");
    }

    public void setBool(final DbObject ths, final boolean v) {
        setObj(ths, v ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean getBool(final DbObject ths) {
        return (Boolean) getObj(ths);
    }

}
