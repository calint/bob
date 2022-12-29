package db.test;

import bob.Titled;
import db.DbObject;
import db.FldStr;

public final class Category extends DbObject implements Titled {
	private static final long serialVersionUID = 1L;

	public final static FldStr name = new FldStr(800); // ? field length is too big for one case, truncate at import
//	public final static Index ixName=new Index(name); // ? key length is max 250 in mysam tables

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
