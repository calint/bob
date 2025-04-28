// reviewed: 2024-08-05
package bob.app;

import b.osltgt;
import b.path;
import b.xwriter;
import bob.Form;

public final class FormFsFile extends Form {

    private final static long serialVersionUID = 1;

    private final path pth;

    public FormFsFile(final path pth) {
        super(null, pth.name(), null, BIT_CLOSE);
        this.pth = pth;
    }

    public String getTitle() {
        return pth.name();
    }

    @Override
    protected void render(final xwriter x) throws Throwable {
        x.tago("div").attr("class", "output").tagoe();
        pth.to(new osltgt(x.outputstream()));
        x.div_();
    }

}
