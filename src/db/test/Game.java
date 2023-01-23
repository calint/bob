package db.test;

import db.DbObject;
import db.FldStr;

public final class Game extends DbObject {
	public final static FldStr name = new FldStr();
	public final static FldStr description = new FldStr(8000);

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getName() {
		return name.getStr(this);
	}

	public void setName(final String v) {
		name.setStr(this, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getDescription() {
		return description.getStr(this);
	}

	public void setDescription(final String v) {
		description.setStr(this, v);
	}

}