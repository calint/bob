package jem;

import java.io.PrintWriter;

import db.FldDbl;

public final class FldDblElem extends ElemFld {
	public FldDblElem(final FldDbl fld) {
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
		out.print("public double get");
		out.print(acc);
		out.println("(){");
		out.print("\t");
		out.print("return getDbl(");
		out.print(fldName);
		out.println(");");
		out.println("}");
		out.println();
		out.print("public void set");
		out.print(acc);
		out.println("(final double v){");
		out.print("\t");
		out.print("set(");
		out.print(fldName);
		out.println(",v);");
		out.println("}");
		out.println();
	}
}
