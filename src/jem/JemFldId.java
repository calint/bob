package jem;

import java.io.PrintWriter;

import db.FldId;

public final class JemFldId extends JemFld {

    public JemFldId(final FldId fld) {
        super(fld);
    }

    @Override
    public void emit(final PrintWriter out) {
        final String fldName = fld.getName();
        final String acc = getAccessorName();

        out.println(HR);
        out.println("public int id(){");
        out.println("\treturn " + fldName + ".getId(this);");
        out.println("}");
        out.println();
        out.println("public int get" + acc + "(){");
        out.println("\treturn " + fldName + ".getId(this);");
        out.println("}");
        out.println();
    }

}
