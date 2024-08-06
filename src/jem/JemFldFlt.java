package jem;

import java.io.PrintWriter;

import db.FldFlt;

public final class JemFldFlt extends JemFld {

    public JemFldFlt(final FldFlt fld) {
        super(fld);
    }

    @Override
    public void emit(final PrintWriter out) {
        final String fldName = fld.getName();
        final String acc = getAccessorName();

        out.println(HR);
        out.println("public float get" + acc + "(){");
        out.println("\treturn " + fldName + ".getFlt(this);");
        out.println("}");
        out.println();
        out.println("public void set" + acc + "(final float v){");
        out.println("\t" + fldName + ".setFlt(this,v);");
        out.println("}");
        out.println();
    }

}
