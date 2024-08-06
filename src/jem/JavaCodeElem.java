package jem;

import java.io.PrintWriter;

public abstract class JavaCodeElem {

    public static final String HR = "// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -";

    public abstract void emit(final PrintWriter out);

}
