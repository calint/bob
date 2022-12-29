package db.test;

import db.DbObject;
import db.FldStr;
import db.Index;

public final class Author extends DbObject {
	private static final long serialVersionUID = 1L;

	public final static FldStr name = new FldStr(250);
	public final static Index ixName=new Index(name);

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getName() {
		return getStr(name);
	}

	public void setName(final String v) {
		set(name, v);
	}
}
