package bob;

import java.util.List;

import b.a;
import b.xwriter;
import bob.ViewTable.SelectReceiverSingle;

public abstract class Form extends a implements Titled {
	static final long serialVersionUID = 1;

	public final static int BIT_SAVE_CLOSE = 1;
	public final static int BIT_SAVE = 2;
	public final static int BIT_CLOSE = 4;
	/** The actions that are enabled on the form. */
	final protected int enabledFormBits;

	public Container ans; // actions container
	public Container scc; // save and close, save, close actions container
	/** Parent object id. */
	protected String parentId;
	/** Object id. */
	protected String objectId;

	private SelectReceiverSingle objectIdReceiver;

	public Form(final String parentId, final String objectId, final int enabledFormBits) {
		this.parentId = parentId;
		this.objectId = objectId;
		this.enabledFormBits = enabledFormBits;
		if ((enabledFormBits & BIT_SAVE_CLOSE) != 0) {
			scc.add(new Action("save and close", "sc"));
		}
		if ((enabledFormBits & BIT_SAVE) != 0) {
			scc.add(new Action("save", "s"));
		}
		if ((enabledFormBits & BIT_CLOSE) != 0) {
			scc.add(new Action("close", "c"));
		}
		final List<Action> actions = getActionsList();
		if (actions == null)
			return;
		for (final Action a : actions) {
			ans.add(a);
		}
	}

	public final String getParentId() {
		return parentId;
	}

	public final String getObjectId() {
		return objectId;
	}

	@Override
	public final void to(final xwriter x) throws Throwable {
		x.script().p("window.onscroll=null;").script_(); // disable infinite scroll event
		x.nl();
		if (!ans.elements().isEmpty()) {
			x.divh(ans, "ac").nl();
		}
		render(x);
		x.divh(scc, "sc").nl();
	}

	@Override
	protected void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
		if (from instanceof Action) {
			final String code = ((Action) from).code();
			if ("sc".equals(code) && (enabledFormBits & BIT_SAVE_CLOSE) != 0) {
				saveAndClose(x);
//				save(x);
//				super.bubble_event(x, this, "close");
				return;
			}
			if ("s".equals(code) && (enabledFormBits & BIT_SAVE) != 0) {
				save(x);
				if (objectIdReceiver != null) {
					objectIdReceiver.onSelect(objectId);
				}
				super.bubble_event(x, this, "updated");
				return;
			}
			if ("c".equals(code) && (enabledFormBits & BIT_CLOSE) != 0) {
				super.bubble_event(x, this, "close");
				return;
			}
			onAction(x, (Action) from);
			return;
		}
		// event unknown by this element
		super.bubble_event(x, from, o);
	}

	protected List<Action> getActionsList() {
		return null;
	}

	protected void onAction(final xwriter x, final Action act) {
	}

	protected final void saveAndClose(final xwriter x) throws Throwable {
		save(x);
		if (objectIdReceiver != null) {
			objectIdReceiver.onSelect(objectId);
		}
		super.bubble_event(x, this, "close");
	}

	protected abstract void render(xwriter x) throws Throwable;

	protected void save(final xwriter x) throws Throwable {
	}

	/** Callback for "save and close" action. */
	public final void x_sc(final xwriter x, final String param) throws Throwable {
		saveAndClose(x);
	}

	/** Triggers a write to the interface when an object has been created. */
	public final void setSelectMode(final SelectReceiverSingle receiver) {
		objectIdReceiver = receiver;
	}
}
