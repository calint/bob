package db.test;

import db.DbObject;
import db.FldBlob;

public final class DataBinary extends DbObject {

    public static final FldBlob data = new FldBlob();

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public byte[] getData() {
        return data.getBlob(this);
    }

    public void setData(final byte[] v) {
        data.setBlob(this, v);
    }
}
