package db.test;

import db.DbField;
import db.DbObject;

public final class FldChars extends DbField {
	public FldChars(final int size, final String def) {
		super("char", size, def == null ? null : def, true, true);
	}

	@Override
	protected Object getDefaultValue() {
		return sqlDefVal;
	}

	public void setChars(final DbObject ths, final String v) {
		setObj(ths, v);
	}

	public String getChars(final DbObject ths) {
		return (String) getObj(ths);
	}

}
