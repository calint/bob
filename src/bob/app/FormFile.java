package bob.app;

import b.osltgt;
import b.path;
import b.xwriter;
import bob.Form;

public final class FormFile extends Form {
	private static final long serialVersionUID = 1L;

	private final path pth;

	public FormFile(final path pth) {
		super(pth.name(), BIT_CLOSE);
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
