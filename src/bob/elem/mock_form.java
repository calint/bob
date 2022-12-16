package bob.elem;

import b.xwriter;
import bob.form;

public class mock_form extends form {
	private static final long serialVersionUID = 1L;

	/** Object id. */
	protected String oid;

	public mock_form(String oid) {
		this.oid = oid;
	}

	public String getTitle() {
		return oid;
	}

	@Override
	public void to(xwriter x) throws Throwable {
		x.p(oid);
	}
}
