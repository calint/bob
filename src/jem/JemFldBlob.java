package jem;

import java.io.PrintWriter;

import db.FldBlob;

public final class JemFldBlob extends JemFld {

    public JemFldBlob(final FldBlob fld) {
        super(fld);
    }

    @Override
    public void emit(final PrintWriter out) {
        final String fldName = fld.getName();
        final String acc = getAccessorName();

        out.println(HR);
        out.println("public byte[]get" + acc + "(){");
        out.println("\treturn " + fldName + ".getBlob(this);");
        out.println("}");
        out.println();
        out.println("public void set" + acc + "(final byte[]v){");
        out.println("\t" + fldName + ".setBlob(this,v);");
        out.println("}");
        out.println();
    }

}
