// reviewed: 2024-08-05
package db;

/** Long field. */
public final class FldLng extends DbField {

	public FldLng(final long defVal) {
		super("bigint", 0, Long.toString(defVal), defVal, false, false);
	}

	public FldLng() {
		this(0);
	}

	public void setLng(final DbObject ths, final long v) {
		setObj(ths, v);
	}

	public long getLng(final DbObject ths) {
		return (Long) getObj(ths);
	}

}
