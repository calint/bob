package b;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import db.DbObject;
import db.FldSerializable;
import db.FldStr;
import db.test.FldChars;

public class dbpathelem extends DbObject {
	public final static FldChars sessionId = new FldChars(32, "");
	public final static FldStr path=new FldStr(255,"");
	public final static FldSerializable elem = new FldSerializable();

	public String getSessionId() {
		return getStr(sessionId);
	}

	public void setSessionId(String v) {
		set(sessionId, v);
	}

	public String getPath() {
		return getStr(path);
	}

	public void setPath(String v) {
		set(path, v);
	}

	public a getElem() {
		final Object v = get(elem);
		if (v == null)
			return null;

		if (v instanceof a) // is it transformed?
			return (a) v;

		// convert from sql representation
		final byte[] ba = getBytesArray(elem);
		try {
			final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ba));
			final a e = (a) ois.readObject();
			ois.close();
			put(elem, e); // put without marking field dirty
			return e;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public void setElem(a v) {
		set(elem, v);
	}

}
