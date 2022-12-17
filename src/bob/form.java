package bob;

import java.util.List;

import b.a;
import b.xwriter;

public abstract class form extends a implements titled {
	static final long serialVersionUID = 1;
	public container ans; // actions
	/** Parent object id. */
	protected String parent_id;
	/** Object id. */
	protected String object_id;
	public action_saveclose asc;
	public action_save as;
	public action_close ac;

	public form(String parent_id, String object_id) {
		this.parent_id = parent_id;
		this.object_id = object_id;
		final List<action> actions = getActionsList();
		if (actions == null)
			return;
		for (action a : actions) {
			ans.add(a);
		}
	}

	public final String getParentId() {
		return parent_id;
	}

	public final String getObjectId() {
		return object_id;
	}

	@Override
	public final void to(xwriter x) throws Throwable {
		x.divh(ans);
		x.nl();
		render(x);
		x.nl().nl();
		asc.to(x);
		x.p(" • ");
		as.to(x);
		x.p(" • ");
		ac.to(x);
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
		if (from instanceof action) {
			onAction(x, (action) from);
			return;
		}
		// event unknown by this element, bubble to parent
		super.bubble_event(x, from, o);
	}

	protected void onAction(xwriter x, action act) {
	}

	protected List<action> getActionsList() {
		return null;
	}

	protected abstract void render(xwriter x) throws Throwable;

	protected abstract void save(xwriter x) throws Throwable;

}
