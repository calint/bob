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

        out.println("public " + accSing + " create" + accSing + "(){");
        out.println("\treturn(" + accSing + ")" + rel.getName() + ".create(this);");
        out.println("}");
        out.println();

        out.println("public DbObjects get" + acc + "(){");
        out.println("\treturn " + rel.getName() + ".get(this);");
        out.println("}");
        out.println();

        out.println("public void delete" + accSing + "(final int id){");
        out.println("\t" + rel.getName() + ".delete(this,id);");
        out.println("}");
        out.println();

        final Class<? extends DbObject> toCls = ((RelAggN) rel).getToClass(); // ? ugly cast
        final String toClsNm = toCls.getName().substring(toCls.getName().lastIndexOf('.') + 1);
        out.println("public void delete" + accSing + "(final " + toClsNm + " o){");
        out.println("\t" + rel.getName() + ".delete(this,o);");
        out.println("}");
        out.println();

        out.println("public void deleteAll" + acc + "(){");
        out.println("\t" + rel.getName() + ".deleteAll(this);");
        out.println("}");
        out.println();
    }

}
