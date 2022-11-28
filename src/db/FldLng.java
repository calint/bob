package db;

import java.util.Map;

/** Long field. */
public final class FldLng extends DbField {
	final private long defval;

	public FldLng(final long def) {
		super("bigint", 0, Long.toString(def), false, false);
		defval = def;
	}

	public FldLng() {
		this(0);
	}

	@Override
	protected void putDefaultValue(final Map<DbField, Object> kvm) {
		kvm.put(this, defval);
	}

}
