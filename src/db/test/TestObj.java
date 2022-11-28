package db.test;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.Timestamp;
import java.util.List;

import db.DbObject;
import db.FldDateTime;
import db.FldStr;

public final class TestObj extends DbObject {
	public final static FldSerializable list = new FldSerializable();
	public final static FldChars md5 = new FldChars(32, "abc");
	public final static FldStr subject = new FldStr(200, "no 'subject'");
	public final static FldDateTime dateTime = new FldDateTime(Timestamp.valueOf("2022-11-28 03:28:00"));

	@SuppressWarnings("unchecked")
	public List<String> getList() {
		final Object v = get(list);
		if (v == null)
			return null;

		if (v instanceof List<?>) // is it transformed?
			return (List<String>) v;

		// convert from sql representation
		final byte[] ba = getBytesArray(list);
		try {
			final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ba));
			final List<String> ls = (List<String>) ois.readObject();
			ois.close();
			put(list, ls); // put without marking field dirty
			return ls;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public void setList(final List<String> v) {
		set(list, v);
	}

	public String getMd5() {
		return getStr(md5);
	}

	public void setMd5(final String v) {
		set(md5, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getSubject() {
		return getStr(subject);
	}

	public void setSubject(final String v) {
		set(subject, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public Timestamp getDateTime() {
		return getTs(dateTime);
	}

	public void setDateTime(final Timestamp v) {
		set(dateTime, v);
	}
}
