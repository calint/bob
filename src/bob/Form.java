//
// reviewed: 2024-08-05
//           2025-04-28
//           2025-05-02
//
package bob;

import java.util.List;

import b.a;
import b.xwriter;
import bob.View.SelectReceiverSingle;

public abstract class Form extends Elem {

    private final static long serialVersionUID = 1;

    public final static int BIT_SAVE_CLOSE = 1;
    public final static int BIT_SAVE = 2;
    public final static int BIT_CLOSE = 4;
    public final static int BIT_CANCEL = 8;

    /** Actions enabled on this form. See BITS_xxx. */
    protected final int enabledFormBits;

    /** Object actions container. */
    public Container oa;

    /** Form "save and close", "save", "close", "cancel" actions container. */
    public Container fa;

    /** The attached views of aggregated objects. */
    public Tabs t;

    /** Id that represents the object this form is rendering. */
    private String objectId;

    /** When select mode this interface will receive the created object id. */
    private SelectReceiverSingle selectReceiverSingle;

    /** Initiation string passed by the view from the query field. */
    private final String initStr;

    /** True if the form has been saved. */
    private boolean isSaved;

    /** True if editing new object. */
    private final boolean isNewObject;

    /**
     * @param idPath          Path to parent of object in context or null if none.
     * @param objectId        String representing the object id. May be null if new.
     * @param initStr         Initial string for constructor to use. View passes the
     *                        query field as initial string.
     * @param enabledFormBits Enable of "save and close", "save", "close" and
     *                        "cancel" actions. See `BIT_xxx`.
     */
    public Form(final IdPath idPath, final String objectId, final String initStr, final int enabledFormBits) {
        super(idPath);

        this.objectId = objectId;
        this.initStr = initStr;
        this.enabledFormBits = enabledFormBits;
        isNewObject = objectId == null;

        if ((enabledFormBits & BIT_SAVE_CLOSE) != 0) {
            fa.add(new Action("save and close", "sc"));
        }
        if ((enabledFormBits & BIT_SAVE) != 0) {
            fa.add(new Action("save", "s"));
        }
        if ((enabledFormBits & BIT_CLOSE) != 0) {
            fa.add(new Action("close", "c"));
        }
        if ((enabledFormBits & BIT_CANCEL) != 0) {
            fa.add(new Action("cancel", "cl"));
        }
    }

    /**
     * Must be called after the form has been constructed to complete the
     * initialization. May be overridden to apply logic before and after
     * initializing actions and views list. Necessary for views and actions that
     * need the object created at `init()`.
     *
     * @return this form.
     */
    public Form init() {
        final List<Action> actions = actionsList();
        if (actions != null) {
            for (final Action a : actions) {
                oa.add(a);
            }
        }
        final List<View> views = viewsList();
        if (views != null) {
            for (final View v : views) {
                t.add(new Tabs.Tab(v.title(), v));
            }
        }
        return this;
    }

    /**
     * Set the receiver for form to call at save after object has been created.
     */
    public final void setSelectMode(final SelectReceiverSingle srs) {
        selectReceiverSingle = srs;
    }

    public final boolean isNewObject() {
        return isNewObject;
    }

    public final String objectId() {
        return objectId;
    }

    public final void objectId(String id) {
        objectId = id;
    }

    /** @return The initializer string supplied at create. */
    public final String initString() {
        return initStr;
    }

    public final boolean isSaved() {
        return isSaved;
    }

    @Override
    public final void to(final xwriter x) throws Throwable {
        x.script().p("window.onscroll=null;").script_().nl();
        // note: disables infinite scroll event

        if (!oa.isEmpty()) {
            // render actions container
            x.divh(oa, "ac").nl();
        }
        render(x);
        if (!fa.isEmpty()) {
            // render form actions
            x.divh(fa, "sc").nl();
        }
        if (!t.isEmpty()) {
            // render tabs
            x.divh(t, "tabs").nl();
        }
    }

    protected final void saveAndClose(final xwriter x) throws Throwable {
        try {
            save(x);
            isSaved = true;
        } catch (final Throwable t) {
            x.xalert(t.getMessage());
            return;
        }
        if (selectReceiverSingle != null) {
            selectReceiverSingle.onSelect(objectId);
        }
        super.bubble_event(x, this, "close");
    }

    /** Callback for "save and close" action. */
    public final void x_sc(final xwriter x, final String param) throws Throwable {
        saveAndClose(x);
    }

    //
    // override methods below for customizations
    //

    @Override
    protected void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
        if (from instanceof Action) {
            final String code = ((Action) from).code();
            if ("sc".equals(code) && (enabledFormBits & BIT_SAVE_CLOSE) != 0) {
                saveAndClose(x);
                return;
            }
            if ("s".equals(code) && (enabledFormBits & BIT_SAVE) != 0) {
                try {
                    save(x);
                    isSaved = true;
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

    /** Called to render form content. */
    protected abstract void render(xwriter x) throws Throwable;

    /**
     * Called to get actions list for for context.
     * 
     * @return List of actions for this form or null.
     */
    protected List<Action> actionsList() {
        return null;
    }

    /** @return List of views of aggregated objects or null. */
    protected List<View> viewsList() {
        return null;
    }

    /** Called when object is saved. */
    protected void save(final xwriter x) throws Throwable {
    }

    /** Called at "cancel" action. */
    protected void cancel(final xwriter x) throws Throwable {
    }

    /** Called when other action is activated. */
    protected void onAction(final xwriter x, final Action act) throws Throwable {
    }

}
