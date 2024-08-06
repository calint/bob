package imp;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** reads CSV file */
public final class Util {

    public static List<String> readList(final Reader reader, final char columnSeparator, final char stringDelim)
            throws IOException {
        final StringBuilder sb = new StringBuilder(1024);
        final ArrayList<String> ls = new ArrayList<String>();
        boolean inString = false;
        sb.setLength(0);
        if (reader.read() != '[')
            throw new RuntimeException("expected '[' to open list.");
        while (true) {
            final int chi = reader.read();
            if (chi == -1)
                return ls;
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
                sb.setLength(0);
                continue;
            }
            if (ch == '\r') { // skip ok
                continue;
            }
            if (ch == ']') {
                ls.add(sb.toString());
                sb.setLength(0);
                break;
            }
            sb.append(ch);
        }
        return ls;
    }

}
