// reviewed: 2024-08-05
//           2025-04-28
package db;

import java.sql.Timestamp;

/** Date time field. */
public final class FldDateTime extends DbField {

    public FldDateTime() {
        this(null);
    }

    public FldDateTime(final Timestamp defVal) {
        super("datetime", 0, defVal == null ? null : FldTs.defValToStr(defVal), defVal, true, true);
    }

    public void setDateTime(final DbObject ths, final Timestamp v) {
        setObj(ths, v);
    }

    public Timestamp getDateTime(final DbObject ths) {
        return (Timestamp) getObj(ths);
    }

}
