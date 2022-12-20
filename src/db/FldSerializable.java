package db;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class FldSerializable extends DbField {
	private static final long serialVersionUID = 1L;
	
	public FldSerializable() {
		super("longblob", 0, null, true, false);
	}

	@Override
	protected void sql_updateValue(final StringBuilder sb, final DbObject o) {
		final Object v = DbObject.getFieldValue(o, this);
		if (v == null) {
			sb.append("null");
			return;
		}
		if (!(v instanceof Serializable))
			throw new RuntimeException("expected serializable object. " + o);

		// if the value has changed then it is kept in java type which is serializable
		final Serializable so = (Serializable) v;
		try {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
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
