package bob.elem;

import b.a;
import b.xwriter;
import bob.data;
import bob.form;

public class form_mock extends form {
	private static final long serialVersionUID = 1L;

	public a title;

	public form_mock(String pid, String oid, String init) {
		super(pid, oid);
		title.set(oid == null ? init : oid);
	}

	public String getTitle() {
		return b.b.isempty(title.str(), "New mock file");
	}

	@Override
	protected void render(xwriter x) throws Throwable {
		x.inptxt(title);
		x.is().xfocus(title).is_();
	}

	@Override
	protected void write(xwriter x) throws Throwable {
		if (oid == null) { // create new
			data.ls.add(title.str());
			return;
		}
		// edit
		for (int i = 0; i < data.ls.size(); i++) {
			final String s = data.ls.get(i);
			if (s.equals(oid)) {
				data.ls.set(i, title.str());
				oid = title.str();
				return;
			}
		}
		throw new RuntimeException("could not find " + oid);
	}
}
