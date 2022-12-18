package bob.app;

import b.osltgt;
import b.path;
import b.xwriter;
import bob.form;

public class form_file extends form {
	private static final long serialVersionUID = 1L;

	private final path pth;

	public form_file(final path pth) {
		super(null, pth.name(), BIT_CLOSE);
		this.pth = pth;
	}

	public String getTitle() {
		return pth.name();
	}

	@Override
	protected void render(final xwriter x) throws Throwable {
		x.divo("output");
		pth.to(new osltgt(x.outputstream()));
		x.div_();
	}
}
