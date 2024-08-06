package jem;

import java.io.PrintWriter;

import db.FldClob;

public final class JemFldClob extends JemFld {

    public JemFldClob(final FldClob fld) {
        super(fld);
    }

    @Override
    public void emit(final PrintWriter out) {
        final String fldName = fld.getName();
        final String acc = getAccessorName();

        out.println(HR);
        out.println("public String get" + acc + "(){");
        out.println("\treturn " + fldName + ".getClob(this);");
        out.println("}");
        out.println();
        out.println("public void set" + acc + "(final String v){");
        out.println("\t" + fldName + ".setClob(this,v);");
        out.println("}");
        out.println();
    }

}
