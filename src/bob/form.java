package bob;

import b.a;
import b.xwriter;

public abstract class form extends a implements titled {
	static final long serialVersionUID = 1;
	public container ans; // actions
	/** Parent object id. */
	protected String pid;
	/** Object id. */
	protected String oid;

	public form(String pid, String oid) {
		this.pid = pid;
		this.oid = oid;
		ans.add(new action_saveclose());
		ans.add(new action_save());
		ans.add(new action_close());
	}

	@Override
	public void to(xwriter x) throws Throwable {
		x.divh(ans);
		x.nl();
		render(x);
	}

	protected abstract void render(xwriter x) throws Throwable;

	@Override
	protected void bubble_event(xwriter x, a from, Object o) throws Throwable {
		if (from instanceof action_saveclose) {
			write(x);
			super.bubble_event(x, this, "close");
			return;
		}
		if (from instanceof action_save) {
			write(x);
			super.bubble_event(x, this, "updated");
			return;
		}
		if (from instanceof action_close) {
			super.bubble_event(x, this, "close");
			return;
		}
		// event unknown by this element, bubble to parent
		super.bubble_event(x, from, o);
	}

	protected abstract void write(xwriter x) throws Throwable;

}
