package jem;

import java.io.PrintWriter;

import db.FldTs;

public final class JemFldTs extends JemFld {
	public JemFldTs(final FldTs fld) {
		super(fld);
	}

//	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
//	public int getNLogins() {
//		return getInt(nlogins);
//	}
//
//	public void setNLogins(int v) {
//		set(nlogins, v);
//	}
//

	@Override
	public void emit(final PrintWriter out) {
		final String fldName = fld.getName();
		final String acc = getAccessorName();

		out.println(HR);
		out.println("public Timestamp get" + acc + "(){");
		out.println("\treturn " + fldName + ".getTs(this);");
		out.println("}");
		out.println();
		out.println("public void set" + acc + "(final Timestamp v){");
		out.println("\t" + fldName + ".setTs(this,v);");
		out.println("}");
		out.println();
	}
}
