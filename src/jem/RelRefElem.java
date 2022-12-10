package jem;

import java.io.PrintWriter;

import db.DbObject;
import db.RelRef;

public class RelRefElem extends ElemRel {
	public RelRefElem(final RelRef rel) {
		super(rel);
	}

//	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
//	public int getGroupPicId() {
//		return groupPic.getId(this);
//	}
//
//	public File getGroupPic() {
//		return (File) groupPic.get(this);
//	}
//
//	public void setGroupPic(int id) {
//		groupPic.set(this, id);
//	}
//

	@Override
	public void emit(final PrintWriter out) {
		final String acc = getAccessorName();

		out.println(HR);
		out.print("public int get");
		out.print(acc);
		out.println("Id(){");
		out.print("\treturn ");
		out.print(rel.getName());
		out.println(".getId(this);");
		out.println("}");
		out.println();

		out.print("public ");
		final Class<? extends DbObject> toCls = ((RelRef) rel).getToClass(); // ? ugly cast
		final String toClsNm = toCls.getName().substring(toCls.getName().lastIndexOf('.') + 1);
		out.print(toClsNm);
		out.print(" get");
		out.print(acc);
		out.println("(){");
		out.print("\treturn(");
		out.print(toClsNm);
		out.print(")");
		out.print(rel.getName());
		out.println(".get(this);");
		out.println("}");
		out.println();

		out.print("public void set");
		out.print(acc);
		out.println("(final int id){");
		out.print("\t");
		out.print(rel.getName());
		out.println(".set(this,id);");
		out.println("}");
		out.println();

		out.print("public void set");
		out.print(acc);
		out.print("(final ");
		out.print(toClsNm);
		out.println(" o){");
		out.print("\t");
		out.print(rel.getName());
		out.println(".set(this,o.id());");
		out.println("}");
		out.println();
	}

}
