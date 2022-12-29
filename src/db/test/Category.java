package db.test;

import db.DbObject;
import db.FldStr;

public final class Category extends DbObject {
	private static final long serialVersionUID = 1L;

	public final static FldStr name = new FldStr(800);
//	public final static Index ixName=new Index(name); // ? key length is max 250 in mysam tables

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getName() {
		return getStr(name);
	}

	public void setName(final String v) {
		set(name, v);
	}
}
