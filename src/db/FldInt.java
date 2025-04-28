// reviewed: 2024-08-05
//           2025-04-28
package db;

/** Integer field. */
public final class FldInt extends DbField {

    public FldInt(final int defVal) {
        super("int", 0, Integer.toString(defVal), defVal, false, false);
    }

    public FldInt() {
        this(0);
    }

    public void setInt(final DbObject ths, final int v) {
        setObj(ths, v);
    }

    public int getInt(final DbObject ths) {
        return (Integer) getObj(ths);
    }

}
