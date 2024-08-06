package jem;

import java.io.PrintWriter;

import db.DbObject;
import db.RelRefN;

public class JemRelRefN extends JemRel {

    public JemRelRefN(final RelRefN rel) {
        super(rel);
    }

    @Override
    public void emit(final PrintWriter out) {
        final String acc = getAccessorName();
        final String accSing = JavaCodeEmitter.getSingulariesForPlurar(acc);

        out.println(HR);

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

        out.println("public void add" + accSing + "(final " + toClsNm + " o){");
        out.println("\t" + rel.getName() + ".add(this,o);");
        out.println("}");
        out.println();

        out.print("public DbObjects get");
        out.print(acc);
        out.println("(){");
        out.print("\treturn ");
        out.print(rel.getName());
        out.println(".get(this);");
        out.println("}");
        out.println();

        out.print("public void remove");
        out.print(accSing);
        out.println("(final int id){");
        out.print("\t");
        out.print(rel.getName());
        out.println(".remove(this,id);");
        out.println("}");
        out.println();

        out.println("public void remove" + accSing + "(final " + toClsNm + " o){");
        out.println("\t" + rel.getName() + ".remove(this,o);");
        out.println("}");
        out.println();

        out.print("public void removeAll");
        out.print(acc);
        out.println("(){");
        out.print("\t");
        out.print(rel.getName());
        out.println(".removeAll(this);");
        out.println("}");
        out.println();
    }

}
