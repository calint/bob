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
    public static final FldLng sizeBytes = new FldLng();
    public static final FldTs createdTs = new FldTs();
    public static final RelAgg data = new RelAgg(DataBinary.class);
    public static final Index ixName = new Index(name);

    public void loadFile(final String path) throws Throwable {
        final DataBinary d = getData(true);
        final java.io.File f = new java.io.File(path);
        if (!f.exists()) {
            throw new RuntimeException("file '" + path + "' not found");
        }
        final long len = f.length();
        setSizeBytes(len);
        // note: does not handle files bigger than 2G
        final byte[] ba = new byte[(int) len];
        final FileInputStream fis = new FileInputStream(f);
        fis.read(ba);
        fis.close();
        d.setData(ba);
    }

    public void writeFile(final String path) throws Throwable {
        final DataBinary d = getData(false);
        if (d == null) {
            return;
        }
        final java.io.File f = new java.io.File(path);
        // note: does not handle files bigger than 2G
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
    public long getSizeBytes() {
        return sizeBytes.getLng(this);
    }

    public void setSizeBytes(final long v) {
        sizeBytes.setLng(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public Timestamp getCreatedTs() {
        return createdTs.getTs(this);
    }

    public void setCreatedTs(final Timestamp v) {
        createdTs.setTs(this, v);
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