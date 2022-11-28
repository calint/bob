package csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** reads CSV file */
public final class CsvReader {
	final private BufferedReader reader;
	final private char columnSeparator;
	final private char stringDelim;
	final private StringBuilder sb = new StringBuilder(1024);

	public CsvReader(final Reader reader) {
		this(reader, ',', '"');
	}

	public CsvReader(final Reader reader, final char columnSeparator, final char stringDelim) {
		this.reader = new BufferedReader(reader);
		this.columnSeparator = columnSeparator;
		this.stringDelim = stringDelim;
	}

	public List<String> nextRecord() throws IOException {
		final ArrayList<String> ls = new ArrayList<String>();
		boolean inString = false;
		sb.setLength(0);
		while (true) {
			final int chi = reader.read();
			if (chi == -1) {
				if (ls.isEmpty())
					return null;
				throw new RuntimeException("unexpected end of stream");
			}
			final char ch = (char) chi;
			if (inString) {
				if (ch == stringDelim) { // example ... "the quote ""hello"" ", ...
					reader.mark(1);
					final int nxtChr = reader.read(); // check if ""
					if (nxtChr == -1)
						throw new RuntimeException("unexpected end of stream");
					if ((char) nxtChr == stringDelim) {
						sb.append(stringDelim);
						continue;
					}
					inString = false;
					reader.reset();
					continue;
				}
				sb.append(ch);
				continue;
			}
			if (ch == columnSeparator) {
				ls.add(sb.toString());
				sb.setLength(0);
				continue;
			}
			if (ch == stringDelim) {
				inString = true;
				continue;
			}
			if (ch == '\r') { // skip ok
				continue;
			}
			if (ch == '\n') {
				ls.add(sb.toString());
				sb.setLength(0);
				break;
			}
			sb.append(ch);
		}
		return ls;
	}

//	public static void main(String[] args) throws Throwable {
//		CsvReader csv = new CsvReader(new FileReader("/home/c/Downloads/prob.csv"), ',', '"');
//		List<String> ls = csv.nextRecord();
//		System.out.println(ls);	
//		while (true) {
//			ls = csv.nextRecord();
//			if (ls == null)
//				break;
//			System.out.println(ls);
//		}
//	}
}
