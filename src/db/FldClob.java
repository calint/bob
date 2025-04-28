//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

/** CLOB field. */
public final class FldClob extends DbField {

    public FldClob() {
        super("longtext", 0, null, null, true, true);
    }

    public void setClob(final DbObject ths, final String v) {
        setObj(ths, v);
    }

    public String getClob(final DbObject ths) {
        return (String) getObj(ths);
    }

}
