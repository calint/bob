package jem;

import java.io.PrintWriter;

import db.FldInt;

public final class JemFldInt extends JemFld {
	public JemFldInt(final FldInt fld) {
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
		out.println("public int get" + acc + "(){");
		out.println("\treturn " + fldName + ".getInt(this);");
		out.println("}");
		out.println();
		out.println("public void set" + acc + "(final int v){");
		out.println("\t" + fldName + ".setInt(this,v);");
		out.println("}");
		out.println();
	}
}
