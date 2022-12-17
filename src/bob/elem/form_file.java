package bob.elem;

import b.osltgt;
import b.path;
import b.xwriter;
import bob.form;

public class form_file extends form {
	private static final long serialVersionUID = 1L;

	private path pth;

	public form_file(path pth) {
		super(null, pth.name(), 0);
		this.pth = pth;
	}

	public String getTitle() {
		return pth.name();
	}

	@Override
	protected void render(xwriter x) throws Throwable {
		x.divo((String) null, "margin-left:auto;margin-right:auto;text-align:left;margin-left:4em;margin-right:4em;border:1px dotted green");
		pth.to(new osltgt(x.outputstream()));
		x.div_();
	}
}
