//
// reviewed: 2024-08-05
//           2025-04-29
//
package bob.app;

import b.osltgt;
import b.path;
import b.xwriter;
import bob.Form;

/** Display Java file. */
public final class FormFsFile extends Form {

    private final static long serialVersionUID = 1;

    private final path pth;

    public FormFsFile(final path pth) {
        super(null, pth.name(), null, BIT_CLOSE);
        this.pth = pth;
    }

    public String title() {
        return pth.name();
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        x.tago("div").attr("class", "output").tagoe();
        // write path to output stream wrapping it with escaper of `<` and `>`
        pth.to(new osltgt(x.outputstream()));
        x.div_().nl();
    }

}
