package db.test;

import db.DbField;

public final class FldChars extends DbField {
	public FldChars(final int size, final String def) {
		super("char", size, def == null ? null : def, true, true);
	}

	@Override
	protected void setDefaultValue(final Object[] values) {
		final String def = getSqlDefaultValue();
		if (def == null)
			return;
		values[slotNbr] = def;
	}

}
