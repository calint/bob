package db;

/** Long field. */
public final class FldLng extends DbField {
	final private Long defval;

	public FldLng(final long def) {
		super("bigint", 0, Long.toString(def), false, false);
		defval = def;
	}

	public FldLng() {
		this(0);
	}

	@Override
	protected Object getDefaultValue() {
		return defval;
	}

	public void setLng(final DbObject ths, final long v) {
		setObj(ths, v);
	}

	public long getLng(final DbObject ths) {
		return (Long) getObj(ths);
	}

}
