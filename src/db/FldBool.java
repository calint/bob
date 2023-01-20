package db;

/** Boolean field. */
public final class FldBool extends DbField {
	final private boolean defval;

	public FldBool(final boolean def) {
		super("bit", 1, def ? "b'1'" : "b'0'", false, false);
		defval = def;
	}

	public FldBool() {
		this(false);
	}

	@Override
	protected void sql_updateValue(final StringBuilder sb, final DbObject o) {
		sb.append(o.getBool(this) ? "b'1'" : "b'0'");
	}

	@Override
	protected void setDefaultValue(final Object[] values) {
		values[slotNbr] = defval;
	}
}
