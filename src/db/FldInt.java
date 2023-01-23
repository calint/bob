package db;

/** Integer field. */
public final class FldInt extends DbField {
	final private Integer defval;

	public FldInt(final int def) {
		super("int", 0, Integer.toString(def), false, false);
		defval = def;
	}

	public FldInt() {
		this(0);
	}

	@Override
	protected Object getDefaultValue() {
		return defval;
	}

	public void setInt(final DbObject ths, final int v) {
		setObj(ths, v);
	}

	public int getInt(final DbObject ths) {
		return (Integer) getObj(ths);
	}
}
