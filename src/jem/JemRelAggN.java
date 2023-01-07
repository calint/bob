package jem;

import java.io.PrintWriter;

import db.DbObject;
import db.RelAggN;

public class JemRelAggN extends JemRel {
	public JemRelAggN(final RelAggN rel) {
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

//		public DbObjects getFiles() {
//			return files.get(this);
//		}

		out.print("public DbObjects get");
		out.print(acc);
		out.println("(){");
		out.print("\treturn ");
		out.print(rel.getName());
		out.println(".get(this);");
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

		final Class<? extends DbObject> toCls = ((RelAggN) rel).getToClass(); // ? ugly cast
		final String toClsNm = toCls.getName().substring(toCls.getName().lastIndexOf('.') + 1);
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

		out.print("public void deleteAll");
		out.print(acc);
		out.println("(){");
		out.print("\t");
		out.print(rel.getName());
		out.println(".deleteAll(this);");
		out.println("}");
		out.println();

	}
}
