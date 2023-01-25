package bob;

import java.util.List;

import b.a;
import b.xwriter;
import bob.View.SelectReceiverSingle;

public abstract class Form extends Elem {
	static final long serialVersionUID = 2;

	public final static int BIT_SAVE_CLOSE = 1;
	public final static int BIT_SAVE = 2;
	public final static int BIT_CLOSE = 4;
	public final static int BIT_CANCEL = 8;
	protected final int enabledFormBits;
	public Container ans; // actions container
	public Container scc; // "save and close", "save", "close" actions container
	public Tabs t; // the attached views of aggregated objects
	protected String objectId; // an id that represents the resource this form is rendering
	private SelectReceiverSingle selectReceiverSingle; // when select mode this interface will receive the created
														// object id
	final private String initStr; // the initiation string passed by the view from the query field
	private boolean hasBeenSaved; // true if the form has been saved

	protected final boolean isNewObject;

	/**
	 * @param objectId        string representing the object.
	 * @param initStr         the initial string for constructor to use. ViewTable
	 *                        passes the query field as initial string.
	 * @param enabledFormBits rendering of "save and close", "save" and "close"
	 *                        actions.
	 */
	public Form(final List<String> idPath, final String objectId, final String initStr, final int enabledFormBits) {
		super(idPath);
		this.objectId = objectId;
		isNewObject = objectId == null;
		this.initStr = initStr;
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
		if ((enabledFormBits & BIT_CANCEL) != 0) {
			scc.add(new Action("cancel", "cl"));
		}
	}

	/**
	 * Must be called after the form has been constructed to complete the
	 * initialization.
	 *
	 * @return this form.
	 */
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

	protected final boolean isNewObject() {
		return isNewObject;
	}

	public final String getObjectId() {
		return objectId;
	}

	public final String getInitStr() {
		return initStr;
	}

	protected final boolean hasBeenSaved() {
		return hasBeenSaved;
	}

	@Override
	public final void to(final xwriter x) throws Throwable {
		x.script().p("window.onscroll=null;").script_().nl(); // disable infinite scroll event
		if (!ans.isEmpty()) {
			x.divh(ans, "ac").nl();
		}
		render(x);
		if (!scc.isEmpty()) {
			x.divh(scc, "sc").nl();
		}
		if (!t.isEmpty()) {
			x.divh(t, "tabs").nl();
		}
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
				try {
					save(x);
					hasBeenSaved = true;
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
			if ("cl".equals(code) && (enabledFormBits & BIT_CANCEL) != 0) {
				cancel(x);
				super.bubble_event(x, this, "close");
				return;
			}
			onAction(x, (Action) from);
			return;
		}
		// event unknown by this element
		super.bubble_event(x, from, o);
	}

	/** @return list of actions for this form. */
	protected List<Action> getActionsList() {
		return null;
	}

	/** @return list of views of aggregated objects. */
	protected List<View> getViewsList() {
		return null;
	}

	/** Called when "cancel" action has been activated. */
	protected void cancel(final xwriter x) throws Throwable {
	}

	/** Called when action is activated. */
	protected void onAction(final xwriter x, final Action act) throws Throwable {
	}

	protected final void saveAndClose(final xwriter x) throws Throwable {
		try {
			save(x);
			hasBeenSaved = true;
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
