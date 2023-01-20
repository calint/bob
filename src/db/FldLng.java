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
}
