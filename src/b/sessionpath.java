package b;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import db.DbObject;
import db.FldSerializable;
import db.FldStr;
import db.Index;

public final class sessionpath extends DbObject {
	public final static FldStr path=new FldStr(255,"");
	public final static FldSerializable elem = new FldSerializable();
	public final static Index ixPath=new Index(path);
	
	public String path() {
		return getStr(path);
	}

	public void path(String v) {
		set(path, v);
	}

	public a elem() {
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

	public void elem(a v) {
		set(elem, v);
	}

}
