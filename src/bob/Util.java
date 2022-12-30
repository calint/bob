package bob;

public final class Util {
	public static String formatSizeInBytes(final long size_B) { // ! one decimal point precision. example 4.1 KB instead
																// of 4 K
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

	public static String toStr(final Object o, final String defaultStr) {
		if (o == null || o.toString().length() == 0)
			return defaultStr;
		return o.toString();
	}

	public static boolean isEmpty(final String s) {
		return s == null || s.isEmpty();
	}
}
