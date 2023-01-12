package db.test;

import bob.Titled;
import db.DbObject;
import db.FldStr;
import db.Index;

public final class Publisher extends DbObject implements Titled {
	public final static FldStr name = new FldStr(250);
	public final static Index ixName = new Index(name);

	public String getTitle() {
		return getName();
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getName() {
		return getStr(name);
	}

	public void setName(final String v) {
		set(name, v);
	}
}
