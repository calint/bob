// reviewed: 2024-08-05
//           2025-04-28
package db;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class FldSerializable extends DbField {
    public FldSerializable() {
        super("longblob", 0, null, null, true, false);
    }

    @Override
    protected void appendSqlUpdateValue(final StringBuilder sb, final DbObject o) {
        final Object v = getObj(o);
        if (v == null) {
            sb.append("null");
            return;
        }
        if (!(v instanceof Serializable)) {
            throw new RuntimeException("expected serializable object. " + o);
        }
        // this method is called if the field has changed
        // in that case this is a java type which is serializable
        final Serializable so = (Serializable) v;
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(256); // ? magic number
            final ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(so);
            oos.close();
            final byte[] ba = bos.toByteArray();
            sb.append("0x");
            sb.ensureCapacity(sb.length() + ba.length * 2);
            FldBlob.appendHexedBytes(sb, ba);
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
