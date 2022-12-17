package bob;

import java.util.List;

import b.a;
import b.xwriter;

public abstract class form extends a implements titled {
	static final long serialVersionUID = 1;
	public container ans; // actions
	public container scc; // save and close, save and close container
	/** Parent object id. */
	protected String parent_id;
	/** Object id. */
	protected String object_id;

	public form(String parent_id, String object_id) {
		this.parent_id = parent_id;
		this.object_id = object_id;
		scc.add(new action("save and clode", "sc"));
		scc.add(new action("save", "s"));
		scc.add(new action("close", "c"));
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
		x.divh(scc);
	}

	@Override
	protected void bubble_event(xwriter x, a from, Object o) throws Throwable {
		if (from instanceof action) {
			final String code = ((action) from).code();
			if ("sc".equals(code)) {
				save(x);
				super.bubble_event(x, this, "close");
				return;
			} else if ("s".endsWith(code)) {
				save(x);
				super.bubble_event(x, this, "updated");
				return;
			} else if ("c".equals(code)) {
				super.bubble_event(x, this, "close");
				return;
			}
			onAction(x, (action) from);
			return;
		}
		// event unknown by this element, bubble to parent
		super.bubble_event(x, from, o);
	}

	protected List<action> getActionsList() {
		return null;
	}

	protected void onAction(xwriter x, action act) {
	}

	protected abstract void render(xwriter x) throws Throwable;

	protected abstract void save(xwriter x) throws Throwable;

}
