package db.test;

import db.DbObject;
import db.FldStr;

public final class Publisher extends DbObject {
	private static final long serialVersionUID = 1L;

	public final static FldStr name = new FldStr(400);

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getName() {
		return getStr(name);
	}

	public void setName(final String v) {
		set(name, v);
	}
}
