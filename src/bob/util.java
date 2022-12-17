package bob;

public class util {
	public final static String formatSizeInBytes(long size_B) {
		long b = size_B;
		if (b < 1024)
			return Long.toString(b) + " B";
		b >>= 10;
		if (b < 1024)
			return Long.toString(b) + " KB";
		b >>= 10;
		if (b < 1024)
			return Long.toString(b) + " MB";
		b >>= 10;
		if (b < 1024)
			return Long.toString(b) + " GB";
		b >>= 10;
		return Long.toString(b) + " TB";
	}
}
