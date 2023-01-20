package db;

/** Integer field. */
public final class FldInt extends DbField {
	final private int defval;

	public FldInt(final int def) {
		super("int", 0, Integer.toString(def), false, false);
		defval = def;
	}

	public FldInt() {
		this(0);
	}

	@Override
	protected void setDefaultValue(final Object[] values) {
		values[slotNbr] = defval;
	}
}
