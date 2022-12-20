package db;

import java.util.Map;

/** Integer field. */
public final class FldInt extends DbField {
	private static final long serialVersionUID = 1L;
	
	final private int defval;

	public FldInt(final int def) {
		super("int", 0, Integer.toString(def), false, false);
		defval = def;
	}

	public FldInt() {
		this(0);
	}

	@Override
	protected void putDefaultValue(final Map<DbField, Object> kvm) {
		kvm.put(this, defval);
	}
}
