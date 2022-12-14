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
	private static final long serialVersionUID = 1L;

	public final static FldStr name = new FldStr(250);
	public final static FldLng size_B = new FldLng();
	public final static FldTs created_ts = new FldTs();
	public final static RelAgg data = new RelAgg(DataBinary.class);
	public final static Index ixName = new Index(name);

	public void loadFile(final String path) throws Throwable {
		final DataBinary d = getData(true);
		final java.io.File f = new java.io.File(path);
		if (!f.exists())
			throw new RuntimeException("file '" + path + "' not found");
		final long len = f.length();
		setSizeB(len);
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
		return getStr(name);
	}

	public void setName(final String v) {
		set(name, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public long getSizeB() {
		return getLng(size_B);
	}

	public void setSizeB(final long v) {
		set(size_B, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public Timestamp getCreatedTs() {
		return getTs(created_ts);
	}

	public void setCreatedTs(final Timestamp v) {
		set(created_ts, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public DataBinary getData(final boolean createIfNone) {
		return (DataBinary) data.get(this, createIfNone);
	}

	public void deleteData() {
		data.delete(this);
	}

}