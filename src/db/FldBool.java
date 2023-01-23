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
		sb.append(getBool(o) ? "b'1'" : "b'0'");
	}

	@Override
	protected Object getDefaultValue() {
		return defval ? Boolean.TRUE : Boolean.FALSE;
	}

	public void setBool(final DbObject ths, final boolean v) {
		setObj(ths, v ? Boolean.TRUE : Boolean.FALSE);
	}

	public boolean getBool(final DbObject ths) {
		return (Boolean) getObj(ths);
	}
}
