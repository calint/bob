package jem;

import java.io.PrintWriter;

import db.FldClob;

public final class FldClobElem extends ElemFld {
	public FldClobElem(final FldClob fld) {
		super(fld);
	}

//	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
//	public String getData() {
//		return getStr(data);
//	}
//
//	public void setData(String v) {
//		set(data, v);
//	}

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
