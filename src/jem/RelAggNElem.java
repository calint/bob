package jem;

import java.io.PrintWriter;

import db.DbObject;
import db.RelAggN;

public class RelAggNElem extends ElemRel {
	public RelAggNElem(final RelAggN rel) {
		super(rel);
	}

	@Override
	public void emit(final PrintWriter out) {
		final String acc = getAccessorName();
		final String accSing = JavaCodeEmitter.getSingulariesForPlurar(acc);
		out.println(HR);

//	public File createFile() {
//		return (File) files.create(this);
//	}
		out.print("public ");
		out.print(accSing);
		out.print(" create");
		out.print(accSing);
		out.println("(){");
		out.print("\treturn(");
		out.print(accSing);
		out.print(")");
		out.print(rel.getName());
		out.println(".create(this);");
		out.println("}");
		out.println();

//		@SuppressWarnings({ "unchecked", "rawtypes" }) // ? uggly
//		public List<File> getFiles(final Query qry, final Order ord, final Limit lmt) {
//			return (List<File>) (List) files.get(this, qry, ord, lmt);
//		}
		final Class<? extends DbObject> toCls = ((RelAggN) rel).getToClass(); // ? ugly cast
		final String toClsNm = toCls.getName().substring(toCls.getName().lastIndexOf('.') + 1);
		out.println("@SuppressWarnings({\"unchecked\",\"rawtypes\"})");
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

//		public int getFilesCount(final Query qry) {
//			return files.getCount(this, qry);
//		}
		out.print("public int get");
		out.print(acc);
		out.println("Count(final Query qry){");
		out.print("\treturn ");
		out.print(rel.getName());
		out.println(".getCount(this,qry);");
		out.println("}");
		out.println();

//		public void deleteFile(int id) {
//			files.delete(this, id);
//		}
		out.print("public void delete");
		out.print(accSing);
		out.println("(final int id){");
		out.print("\t");
		out.print(rel.getName());
		out.println(".delete(this,id);");
		out.println("}");
		out.println();

		out.print("public void delete");
		out.print(accSing);
		out.print("(final ");
		out.print(toClsNm);
		out.println(" o){");
		out.print("\t");
		out.print(rel.getName());
		out.println(".delete(this,o.id());");
		out.println("}");
		out.println();

	}
}
