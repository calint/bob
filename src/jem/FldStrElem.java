package jem;

import java.io.PrintWriter;

import db.FldStr;

public final class FldStrElem extends ElemFld {
	public FldStrElem(final FldStr fld) {
		super(fld);
	}

//	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
//	public String getName() {
//		return getStr(name);
//	}
//
//	public void setName(String v) {
//		set(name, v);
//	}
//

	@Override
	public void emit(final PrintWriter out) {
		final String fldName = fld.getName();
		final String acc = getAccessorName();

		out.println(HR);
		out.print("public String get");
		out.print(acc);
		out.println("(){");
		out.print("\t");
		out.print("return getStr(");
		out.print(fldName);
		out.println(");");
		out.println("}");
		out.println();
		out.print("public void set");
		out.print(acc);
		out.println("(final String v){");
		out.print("\t");
		out.print("set(");
		out.print(fldName);
		out.println(",v);");
		out.println("}");
		out.println();
	}
}
