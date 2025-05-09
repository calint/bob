//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

/** BLOB field. */
public final class FldBlob extends DbField {

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public FldBlob() {
        super("longblob", 0, null, null, true, false);
    }

    @Override
    protected void appendSqlUpdateValue(final StringBuilder sb, final DbObject o) {
        sb.append("0x");
        final byte[] data = getBlob(o);
        final int cap = sb.length() + data.length * 2;
        sb.ensureCapacity(cap);
        appendHexedBytes(sb, data);
    }

    public void setBlob(final DbObject ths, final byte[] v) {
        setObj(ths, v);
    }

    public byte[] getBlob(final DbObject ths) {
        return (byte[]) getObj(ths);
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

}
