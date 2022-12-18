package jem;

import java.io.PrintWriter;

import db.FldId;

public final class FldIdElem extends ElemFld {
	public FldIdElem(final FldId fld) {
		super(fld);
	}

//	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
//	final public int id() {
//		return getInt(id);
//	}
//
//	final public int getId() {
//		return id();
//	}

	@Override
	public void emit(final PrintWriter out) {
		final String fldName = fld.getName();
		final String acc = getAccessorName();

		out.println(HR);
		out.print("public int id");
		out.println("(){");
		out.print("\treturn getInt(");
		out.print(fldName);
		out.println(");");
		out.println("}");
		out.println();
		out.print("public int get");
		out.print(acc);
		out.println("(){");
		out.println("\treturn id();");
		out.println("}");
	}
}
