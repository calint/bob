package db.test;

import db.DbObject;
import db.FldStr;

public final class Game extends DbObject {
	public final static FldStr name = new FldStr();
	public final static FldStr description = new FldStr(8000);

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getName() {
		return getStr(name);
	}

	public void setName(final String v) {
		set(name, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getDescription() {
		return getStr(description);
	}

	public void setDescription(final String v) {
		set(description, v);
	}

}