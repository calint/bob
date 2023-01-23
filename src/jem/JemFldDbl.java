package jem;

import java.io.PrintWriter;

import db.FldDbl;

public final class JemFldDbl extends JemFld {
	public JemFldDbl(final FldDbl fld) {
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
		out.println("public double get" + acc + "(){");
		out.println("\treturn " + fldName + ".getDbl(this);");
		out.println("}");
		out.println();
		out.println("public void set" + acc + "(final double v){");
		out.println("\t" + fldName + ".setDbl(this,v);");
		out.println("}");
		out.println();
	}
}
