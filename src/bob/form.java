package bob;

import b.a;
import b.xwriter;

public abstract class form extends a implements titled {
	static final long serialVersionUID = 1;
	public container ans; // actions
	/** Parent object id. */
	protected String parent_id;
	/** Object id. */
	protected String object_id;

	public form(String parent_id, String object_id) {
		this.parent_id = parent_id;
		this.object_id = object_id;
		ans.add(new action_saveclose());
		ans.add(new action_save());
		ans.add(new action_close());
	}

	public final String getParentId() {
		return parent_id;
	}

	public final String getObjectId() {
		return object_id;
	}

	@Override
	public final void to(xwriter x) throws Throwable {
		render(x);
		x.nl().nl();	
		x.divh(ans);
	}

	@Override
	protected void bubble_event(xwriter x, a from, Object o) throws Throwable {
		if (from instanceof action_saveclose) {
			save(x);
			super.bubble_event(x, this, "close");
			return;
		}
		if (from instanceof action_save) {
			save(x);
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

	protected abstract void render(xwriter x) throws Throwable;

	protected abstract void save(xwriter x) throws Throwable;

}
