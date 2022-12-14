package db;

/** BLOB field. */
public final class FldBlob extends DbField {
	private static final long serialVersionUID = 1L;

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
		for (final byte element : bytes) {
			final int v = element & 0xFF;
			hex[0] = HEX_ARRAY[v >>> 4];
			hex[1] = HEX_ARRAY[v & 0x0F];
			sb.append(hex);
		}
	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
}
