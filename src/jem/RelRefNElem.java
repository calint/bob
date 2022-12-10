package jem;

import java.io.PrintWriter;

import db.DbObject;
import db.RelRefN;

public class RelRefNElem extends ElemRel {
	public RelRefNElem(final RelRefN rel) {
		super(rel);
	}

	@Override
	public void emit(final PrintWriter out) {
		final String acc = getAccessorName();
		final String accSing = JavaCodeEmitter.getSingulariesForPlurar(acc);
		out.println(HR);
//		// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
//		public void addRefFile(int id) {
//			refFiles.add(this, id);
//		}
		out.print("public void add");
		out.print(accSing);
		out.println("(final int id){");
		out.print("\t");
		out.print(rel.getName());
		out.println(".add(this,id);");
		out.println("}");
		out.println();

		final Class<? extends DbObject> toCls = ((RelRefN) rel).getToClass(); // ? ugly cast
		final String toClsNm = toCls.getName().substring(toCls.getName().lastIndexOf('.') + 1);

		out.print("public void add");
		out.print(accSing);
		out.print("(final ");
		out.print(toClsNm);
		out.println(" o){");
		out.print("\t");
		out.print(rel.getName());
		out.println(".add(this,o.id());");
		out.println("}");
		out.println();

//		@SuppressWarnings({ "unchecked", "rawtypes" })
//		public List<File> getRefFiles(final Query qry, final Order ord, final Limit lmt) {
//			return (List<File>) (List) refFiles.get(this, qry, ord, lmt);
//		}
		out.println("@SuppressWarnings({ \"unchecked\", \"rawtypes\" })");
		out.print("public ");
		out.print("List<");
		out.print(toClsNm);
		out.print(">");
		out.print("get");
		out.print(acc);
		out.println("(final Query qry,final Order ord,final Limit lmt){");
		out.print("\treturn(List<");
		out.print(toClsNm);
		out.print(">)(List)");
		out.print(rel.getName());
		out.println(".get(this,qry,ord,lmt);");
		out.println("}");
		out.println();

//		public int getRefFilesCount(final Query qry) {
//			return refFiles.getCount(this, qry);
//		}
		out.print("public int get");
		out.print(acc);
		out.println("Count(final Query qry){");
		out.print("\treturn ");
		out.print(rel.getName());
		out.println(".getCount(this,qry);");
		out.println("}");
		out.println();

//	public void removeRefFile(int id) {
//		refFiles.remove(this, id);
//	}
		out.print("public void remove");
		out.print(accSing);
		out.println("(final int id){");
		out.print("\t");
		out.print(rel.getName());
		out.println(".remove(this,id);");
		out.println("}");
		out.println();

		out.print("public void remove");
		out.print(accSing);
		out.print("(final ");
		out.print(toClsNm);
		out.println(" o){");
		out.print("\t");
		out.print(rel.getName());
		out.println(".remove(this,o.id());");
		out.println("}");
		out.println();
	}
}
