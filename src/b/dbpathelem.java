package b;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import db.DbObject;
import db.FldSerializable;
import db.FldStr;

public class dbpathelem extends DbObject {
	public final static FldStr path=new FldStr(255,"");
	public final static FldSerializable elem = new FldSerializable();

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
			try {
				final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ba));
				final a e = (a) ois.readObject();
				ois.close();
				put(elem, e); // put without marking field dirty
				return e;
			}catch(Throwable t) {
				return null; // ? what to do?
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public void setElem(a v) {
		set(elem, v);
	}

}
