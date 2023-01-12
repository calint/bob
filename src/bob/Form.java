package bob;

import java.util.List;

import b.a;
import b.xwriter;
import bob.View.SelectReceiverSingle;

public abstract class Form extends a implements Titled {
	static final long serialVersionUID = 1;

	public final static int BIT_SAVE_CLOSE = 1;
	public final static int BIT_SAVE = 2;
	public final static int BIT_CLOSE = 4;
	protected final int enabledFormBits;
	public Container ans; // actions container
	public Container scc; // "save and close", "save", "close" actions container
	public Tabs t; // the attached views of aggregated objects
	protected String objectId; // an id that represents the resource this form is rendering
	private SelectReceiverSingle selectReceiverSingle; // when select mode this interface will receive the created
														// object id

	public Form(final String objectId, final int enabledFormBits) {
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
	}

	/** Must be called after the form has been constructed to complete the initialization.
	 * @return this form. */
	public Form init() {
		final List<Action> actions = getActionsList();
		if (actions != null) {
			for (final Action a : actions) {
				ans.add(a);
			}
		}
		final List<View> views = getViewsList();
		if (views != null) {
			for (final View v : views) {
				t.add(new Tabs.Tab(v.getTitle(), v));
			}
		}
		return this;
	}

	public final String getObjectId() {
		return objectId;
	}

	@Override
	public final void to(final xwriter x) throws Throwable {
//		preRender(x);
		x.script().p("window.onscroll=null;").script_().nl(); // disable infinite scroll event
		if (!ans.elements().isEmpty()) {
			x.divh(ans, "ac").nl();
		}
		render(x);
		if (!scc.elements().isEmpty()) {
			x.divh(scc, "sc").nl();
		}
		x.divh(t, "tabs").nl();
		// ! render tabbed views from getViewsList()
	}

//	protected void preRender(final xwriter x) {
//	}
//
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
				try {
					save(x);
				} catch (final Throwable t) {
					x.xalert(t.getMessage());
					return;
				}
				if (selectReceiverSingle != null) {
					selectReceiverSingle.onSelect(objectId);
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

	/** @return user actions for this form. */
	protected List<Action> getActionsList() {
		return null;
	}

	/** @return list of views of aggregated objects. */
	protected List<View> getViewsList() {
		return null;
	}

	/** Called when action is activated. */
	protected void onAction(final xwriter x, final Action act) throws Throwable {
	}

	protected final void saveAndClose(final xwriter x) throws Throwable {
		try {
			save(x);
		} catch (final Throwable t) {
			x.xalert(t.getMessage());
			return;
		}
		if (selectReceiverSingle != null) {
			selectReceiverSingle.onSelect(objectId);
		}
		super.bubble_event(x, this, "close");
	}

	/** Called to render form content. */
	protected abstract void render(xwriter x) throws Throwable;

	/** Called when object is saved. */
	protected void save(final xwriter x) throws Throwable {
	}

	/** Callback for "save and close" action. */
	public final void x_sc(final xwriter x, final String param) throws Throwable {
		saveAndClose(x);
	}

	/**
	 * Form calls onSelect on interface at save after object has been created.
	 */
	public final void setSelectMode(final SelectReceiverSingle srs) {
		selectReceiverSingle = srs;
	}
}
