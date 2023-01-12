package db;

import java.util.Map;

/** String field. */
public final class FldStr extends DbField {
	public final static int MAX_SIZE = 65535; // ? this is mysql specific

	public FldStr() {
		this(250, null, true);
	}

	public FldStr(final int size) {
		this(size, null, true);
	}

	public FldStr(final String def) {
		this(250, def, true);
	}

	public FldStr(final int size, final String def) {
		this(size, def, true);
	}

	public FldStr(final int size, final String def, final boolean allowNull) {
		super("varchar", size, def, allowNull, true);
		if (size > MAX_SIZE)
			throw new RuntimeException("size " + size + " exceeds maximum of " + MAX_SIZE);
	}

	@Override
	protected void putDefaultValue(final Map<DbField, Object> kvm) {
		if (defVal == null)
			return;

		kvm.put(this, defVal);
	}
}
