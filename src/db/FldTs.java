// reviewed: 2024-08-05
//           2025-04-28
package db;

import java.sql.Timestamp;

/** Timestamp field. */
public final class FldTs extends DbField {

    public FldTs(final Timestamp defVal) {
        super("timestamp", 0, defVal == null ? null : defValToStr(defVal), defVal, true, true);
    }

    public FldTs() {
        this(null);
    }

    public void setTs(final DbObject ths, final Timestamp v) {
        setObj(ths, v);
    }

    public Timestamp getTs(final DbObject ths) {
        return (Timestamp) getObj(ths);
    }

    public static String defValToStr(final Timestamp def) {
        String s = def.toString();
        if (s.endsWith(".0")) {
            // java.sql.Timestamp adds .0 at the end. mysql default value does not.
            s = s.substring(0, s.length() - 2);
        }
        return s;
    }

}
