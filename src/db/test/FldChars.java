package db.test;

import java.util.Map;

import db.DbField;

public final class FldChars extends DbField {

	public FldChars(final int size, final String def) {
		super("char", size, def == null ? null : def, true, true);
	}

	@Override
	protected void putDefaultValue(final Map<DbField, Object> kvm) {
		final String def = getDefaultValue();
		if (def == null) {
			return;
		}
		kvm.put(this, def);
	}

}
