package db;

/** BLOB field. */
public final class FldBlob extends DbField {
	public FldBlob() {
		super("longblob", 0, null, true, false);
	}

	@Override
	protected void sql_updateValue(final StringBuilder sb, final DbObject o) {
		sb.append("0x");
		final byte[] data = o.getBytesArray(this);
		final int cap = sb.length() + data.length * 2;
		sb.ensureCapacity(cap);
		appendHexedBytes(sb, data);
	}

	public static void appendHexedBytes(final StringBuilder sb, final byte[] bytes) {
		final char[] hex = new char[2];
		for (int i = 0; i < bytes.length; i++) {
			final int v = bytes[i] & 0xFF;
			hex[0] = HEX_ARRAY[v >>> 4];
			hex[1] = HEX_ARRAY[v & 0x0F];
			sb.append(hex);
		}
	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
}
