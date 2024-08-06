package db.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Timestamp;

import bob.Titled;
import db.DbObject;
import db.FldLng;
import db.FldStr;
import db.FldTs;
import db.Index;
import db.RelAgg;

public final class File extends DbObject implements Titled {
    public static final FldStr name = new FldStr(250);
    public static final FldLng size_B = new FldLng();
    public static final FldTs created_ts = new FldTs();
    public static final RelAgg data = new RelAgg(DataBinary.class);
    public static final Index ixName = new Index(name);

    public void loadFile(final String path) throws Throwable {
        final DataBinary d = getData(true);
        final java.io.File f = new java.io.File(path);
        if (!f.exists())
            throw new RuntimeException("file '" + path + "' not found");
        final long len = f.length();
        setSize_B(len);
        // note does not handle files bigger than 4G
        final byte[] ba = new byte[(int) len];
        final FileInputStream fis = new FileInputStream(f);
        fis.read(ba);
        fis.close();
        d.setData(ba);
    }

    public void writeFile(final String path) throws Throwable {
        final DataBinary d = getData(false);
        if (d == null)
            return;
        final java.io.File f = new java.io.File(path);
        // note does not handle files bigger than 4G
        final byte[] ba = d.getData();
        final FileOutputStream fos = new FileOutputStream(f);
        fos.write(ba);
        fos.close();
    }

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

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public long getSize_B() {
        return size_B.getLng(this);
    }

    public void setSize_B(final long v) {
        size_B.setLng(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public Timestamp getCreated_ts() {
        return created_ts.getTs(this);
    }

    public void setCreated_ts(final Timestamp v) {
        created_ts.setTs(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public int getDataId() {
        return data.getId(this);
    }

    public DataBinary getData(final boolean createIfNone) {
        return (DataBinary) data.get(this, createIfNone);
    }

    public void deleteData() {
        data.delete(this);
    }
}