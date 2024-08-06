package db.test;

import bob.Titled;
import db.DbObject;
import db.FldStr;
import db.Index;

public final class Author extends DbObject implements Titled {
    public final static FldStr name = new FldStr(250);
    public final static Index ixName = new Index(name);

    public String getTitle() {
        return getName();
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getName() {
        return name.getStr(this);
    }

    public void setName(final String v) {
        name.setStr(this, v);
    }
}
