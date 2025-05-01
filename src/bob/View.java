//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;

public abstract class View extends Elem {

    private final static long serialVersionUID = 1;

    public final static int BIT_CREATE = 1;
    public final static int BIT_DELETE = 2;
    public final static int BIT_SEARCH = 4;
    public final static int BIT_SELECT = 8;

    private final static TypeInfo defaultTypeInfo = new TypeInfo("object", "objects");

    /** The actions that are enabled in the view. */
    protected final int enabledViewBits;

    /** True if view renders for selection item(s) */
    private boolean isSelectMode;

    /** True if view renders for selection of multiple items. */
    private boolean isSelectModeMulti;

    /** Receiver of selection of multiple items. */
    private SelectReceiverMulti selectReceiverMulti;

    /** Receiver of selection of single item. */
    private SelectReceiverSingle selectReceiverSingle;

    /** Name and plural of the object type. */
    private final TypeInfo typeInfo;

    /** The query field that all views have. */
    public a q;

    /**
     * @param ti     TypeInfo may be null and default "object"/"objects" is used.
     * @param idPath May be null.
     */
    public View(final TypeInfo ti, final IdPath idPath, final int enabledBits) {
        super(idPath);

        enabledViewBits = enabledBits;
        typeInfo = ti == null ? defaultTypeInfo : ti;
    }

    /** Must be called after constructor to complete initialization. */
    public View init() throws Throwable {
        return this;
    }

    public final TypeInfo typeInfo() {
        return typeInfo;
    }

    public final static class TypeInfo implements Serializable {

        private final static long serialVersionUID = 1;

        private final String name;
        private final String namePlural;

        public TypeInfo(final String name, final String namePlural) {
            this.name = name;
            this.namePlural = namePlural;
        }

        public String name() {
            return name;
        }

        public String namePlural() {
            return namePlural;
        }
    }

    interface SelectReceiverMulti extends Serializable {
        void onSelect(Set<String> selectedIds);
    }

    interface SelectReceiverSingle extends Serializable {
        void onSelect(String selectedId);
    }

    public final boolean isSelectMode() {
        return isSelectMode;
    }

    public final boolean isSelectModeMulti() {
        return isSelectModeMulti;
    }

    public final void setSelectMode(final Set<String> selectedIds, final SelectReceiverMulti sr) {
        isSelectMode = true;
        isSelectModeMulti = true;
        selectReceiverMulti = sr;
        final Set<String> selection = selectedIds();
        selection.clear();
        selection.addAll(selectedIds);
    }

    public final void setSelectMode(final String selectedId, final SelectReceiverSingle sr) {
        isSelectMode = true;
        isSelectModeMulti = false;
        selectReceiverSingle = sr;
    }

    public final SelectReceiverMulti getSelectReceiverMulti() {
        return selectReceiverMulti;
    }

    public final SelectReceiverSingle getSelectReceiverSingle() {
        return selectReceiverSingle;
    }

    //
    // customize methods below
    //

    /** @return Objects in this view */
    protected abstract List<?> objectsList();

    /**
     * @return Objects per page or 0 if paging is disabled.
     */
    protected int objectsPerPageCount() {
        return 0;
    }

    /**
     * If paging is enabled this will be called before `objectsList()`.
     * 
     * @return Number of objects in the list
     */
    protected int objectsCount() {
        return 0;
    }

    /**
     * If implementor adds additional actions get the list by calling
     * `super.actionsList()` and add actions.
     */
    protected List<Action> actionsList() {
        final ArrayList<Action> ls = new ArrayList<Action>();
        if ((enabledViewBits & BIT_CREATE) != 0) {
            ls.add(new Action("create " + typeInfo.name(), "create"));
        }
        if ((enabledViewBits & BIT_DELETE) != 0) {
            ls.add(new Action("delete selected " + typeInfo.namePlural(), "delete"));
        }
        return ls;
    }

    /**
     * Called to get the id from an object. Used to link views to other views or
     * forms.
     */
    protected abstract String idFrom(Object obj);

    protected abstract void onActionCreate(xwriter x, String initStr) throws Throwable;

    protected abstract void onActionDelete(xwriter x) throws Throwable;

    protected abstract void onAction(xwriter x, Action act) throws Throwable;

    protected abstract Set<String> selectedIds();

    /** Refresh view. */
    protected void refresh(xwriter x) throws Throwable {
    }

    @Override
    protected void bubble_event(final xwriter x, final a from, final Object o) throws Throwable {
        if (from instanceof Action) {
            final String code = ((Action) from).code();
            if ("create".equals(code) && (enabledViewBits & BIT_CREATE) != 0) {
                onActionCreate(x, q.str());
                return;
            }
            if ("delete".equals(code) && (enabledViewBits & BIT_DELETE) != 0) {
                if (selectedIds().isEmpty()) {
                    x.xalert("No " + typeInfo().namePlural() + " selected.");
                    return;
                }
                onActionDelete(x);
                refresh(x);
                return;
            }
            onAction(x, (Action) from);
            refresh(x);
            return;
        }
        // event unknown by this element
        super.bubble_event(x, from, o);
    }
}
