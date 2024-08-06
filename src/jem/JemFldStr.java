package jem;

import java.io.PrintWriter;

import db.FldStr;

public final class JemFldStr extends JemFld {

    public JemFldStr(final FldStr fld) {
        super(fld);
    }

    @Override
    public void emit(final PrintWriter out) {
        final String fldName = fld.getName();
        final String acc = getAccessorName();

        out.println(HR);
        out.println("public String get" + acc + "(){");
        out.println("\treturn " + fldName + ".getStr(this);");
        out.println("}");
        out.println();
        out.println("public void set" + acc + "(final String v){");
        out.println("\t" + fldName + ".setStr(this,v);");
        out.println("}");
        out.println();
    }

}
