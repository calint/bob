package jem;

import java.io.PrintWriter;

import db.FldBool;

public final class JemFldBool extends JemFld {

    public JemFldBool(final FldBool fld) {
        super(fld);
    }

    @Override
    public void emit(final PrintWriter out) {
        final String fldName = fld.getName();
        final String acc = getAccessorName();

        out.println(HR);
        out.println("public boolean is" + acc + "(){");
        out.println("\treturn " + fldName + ".getBool(this);");
        out.println("}");
        out.println();
        out.println("public void set" + acc + "(final boolean v){");
        out.println("\t" + fldName + ".setBool(this,v);");
        out.println("}");
        out.println();
    }

}
