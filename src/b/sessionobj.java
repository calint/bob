// reviewed: 2024-08-05
package b;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import db.DbObject;
import db.FldSerializable;
import db.FldStr;
import db.Index;

/** Serialized persistent session object bound to a path. */
public final class sessionobj extends DbObject {
    public final static FldStr path = new FldStr(250, "");
    public final static FldSerializable object = new FldSerializable();
    public final static Index ixPath = new Index(path);

    public String path() {
        return path.getStr(this);
    }

    public void path(final String v) {
        path.setStr(this, v);
    }

    public Object object() {
        final Object v = object.getObj(this);
        if (v == null) {
            return null;
        }
        if (!(v instanceof byte[])) {
            return v;
        }
        // object has not been deserialized
        final byte[] ba = (byte[]) v;
        try {
            final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ba));
            final Object o = ois.readObject();
            ois.close();
            object.putObj(this, o); // put without marking field dirty
            return o;
        } catch (final Throwable t) {
            return null; // ? what to do?
        }
    }

    public void object(final Serializable v) {
        object.setObj(this, v);
    }
}
