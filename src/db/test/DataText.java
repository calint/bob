package db.test;

import db.DbObject;
import db.FldClob;
import db.IndexFt;

public final class DataText extends DbObject {
    public final static FldClob meta = new FldClob();
    public final static FldClob data = new FldClob();

    public final static IndexFt ft = new IndexFt(meta, data);

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getMeta() {
        return meta.getClob(this);
    }

    public void setMeta(final String v) {
        meta.setClob(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getData() {
        return data.getClob(this);
    }

    public void setData(final String v) {
        data.setClob(this, v);
    }
}