package jem;

import java.io.PrintWriter;

import db.FldBlob;

public final class JemFldBlob extends JemFld {
	public JemFldBlob(final FldBlob fld) {
		super(fld);
	}

//	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
//	public byte[] getData() {
//		return getBytesArray(data);
//	}
//
//	public void setData(byte[] v) {
//		set(data, v);
//	}

	@Override
	public void emit(final PrintWriter out) {
		final String fldName = fld.getName();
		final String acc = getAccessorName();

		out.println(HR);
		out.print("public byte[]get");
		out.print(acc);
		out.println("(){");
		out.print("\t");
		out.print("return getBytesArray(");
		out.print(fldName);
		out.println(");");
		out.println("}");
		out.println();
		out.print("public void set");
		out.print(acc);
		out.println("(final byte[]v){");
		out.print("\t");
		out.print("set(");
		out.print(fldName);
		out.println(",v);");
		out.println("}");
		out.println();
	}
}
