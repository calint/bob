//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import b.osltgt;
import b.xwriter;
import bob.Form;
import db.DbClass;

public final class FormDbClass extends Form {

    private final static long serialVersionUID = 1;

    private final String javaClassName;

    public FormDbClass(final DbClass dbclass) {
        super(null, dbclass.getJavaClass().getName(), null, BIT_CLOSE);
        javaClassName = dbclass.getJavaClass().getName();
    }

    public String title() {
        return javaClassName;
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        // open div tag with class attribute and close it.
        x.tago("div").attr("class", "output").tagoe();
        jem.Main.main(javaClassName, new osltgt(x.outputstream()));
        x.div_().nl();
    }

}
