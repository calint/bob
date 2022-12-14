package b;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import db.DbObject;
import db.FldSerializable;
import db.FldStr;
import db.Index;

public final class sessionobj extends DbObject{
	private static final long serialVersionUID=1L;

	public final static FldStr path=new FldStr(250,"");
	public final static FldSerializable object=new FldSerializable();
	public final static Index ixPath=new Index(path);

	public String path(){
		return getStr(path);
	}

	public void path(final String v){
		set(path,v);
	}

	public Object object(){
		final Object v=get(object);
		if(v==null)
			return null;

		if(!(v instanceof byte[]))
			return v;

		// convert from sql representation
		final byte[] ba=(byte[])v;
		try{
			final ObjectInputStream ois=new ObjectInputStream(new ByteArrayInputStream(ba));
			final Object o=ois.readObject();
			ois.close();
			put(object,o); // put without marking field dirty
			return o;
		}catch(final Throwable t){
			return null; // ? what to do?
		}
	}

	public void object(final Serializable v){
		set(object,v);
	}

}
