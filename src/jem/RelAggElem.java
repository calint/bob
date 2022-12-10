package jem;

import java.io.PrintWriter;

import db.DbObject;
import db.RelAgg;

public class RelAggElem extends ElemRel {
	public RelAggElem(final RelAgg rel) {
		super(rel);
	}

	@Override
	public void emit(final PrintWriter out) {
		final String acc = getAccessorName();

		out.println(HR);
//		// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
//		public int getProfilePicId() {
//			return profilePic.getId(this);
//		}
		out.print("public int get");
		out.print(acc);
		out.println("Id(){");
		out.print("\treturn ");
		out.print(rel.getName());
		out.println(".getId(this);");
		out.println("}");
		out.println();

//		public File getProfilePic(final boolean createIfNone) {
//			return (File) profilePic.get(this, createIfNone);
//		}
		out.print("public ");
		final Class<? extends DbObject> toCls = ((RelAgg) rel).getToClass(); // ? ugly cast
		final String toClsNm = toCls.getName().substring(toCls.getName().lastIndexOf('.') + 1);
		out.print(toClsNm);
		out.print(" get");
		out.print(acc);
		out.println("(final boolean createIfNone){");
		out.print("\treturn(");
		out.print(toClsNm);
		out.print(")");
		out.print(rel.getName());
		out.println(".get(this,createIfNone);");
		out.println("}");
		out.println();

//		public void deleteProfilePic() {
//			profilePic.delete(this);
//		}
		out.print("public void delete");
		out.print(acc);
		out.println("(){");
		out.print("\t");
		out.print(rel.getName());
		out.println(".delete(this);");
		out.println("}");
		out.println();
	}

}
