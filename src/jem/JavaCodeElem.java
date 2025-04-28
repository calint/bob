//
// reviewed: 2025-04-28
//
package jem;

import java.io.PrintWriter;

public abstract class JavaCodeElem {

    public final static String HR = "// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -";

    public abstract void emit(final PrintWriter out);

}
