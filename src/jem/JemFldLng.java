//
// reviewed: 2025-04-28
//
package jem;

import java.io.PrintWriter;

import db.FldLng;

public final class JemFldLng extends JemFld {

    public JemFldLng(final FldLng fld) {
        super(fld);
    }

    @Override
    public void emit(final PrintWriter out) {
        final String fldName = fld.getName();
        final String acc = getAccessorName();

        out.println(HR);
        out.println("public long get" + acc + "(){");
        out.println("\treturn " + fldName + ".getLng(this);");
        out.println("}");
        out.println();
        out.println("public void set" + acc + "(final long v){");
        out.println("\t" + fldName + ".setLng(this,v);");
        out.println("}");
        out.println();
    }

}
