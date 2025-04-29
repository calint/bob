//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import b.xwriter;

public abstract class View extends Elem {

    private final static long serialVersionUID = 1;

    public final static int BIT_CREATE = 1;
    public final static int BIT_DELETE = 2;
    public final static int BIT_SEARCH = 4;
    public final static int BIT_SELECT = 8;

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
    private final View.TypeInfo typeInfo;

    public View(final TypeInfo ti, final List<String> idPath, final int enabledBits) {
        super(idPath);

        enabledViewBits = enabledBits;
        typeInfo = ti == null ? new TypeInfo("object", "objects") : ti;
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
    // customize methods below for customizations
    //

    protected List<Action> actionsList() {
        return null;
    }

    /**
     * @return Objects per page or 0 if paging is disabled.
     */
    protected abstract int objectsPerPageCount();

    /**
     * If paging is enabled this will be called before getObjectsList().
     * 
     * @return Number of objects in the list
     */
    protected abstract int objectsCount();

    /** @return Objects in this list */
    protected abstract List<?> objectsList();

    /**
     * Called to get the id from an object. Used to link views to other views or
     * forms.
     */
    protected abstract String idFrom(Object obj);

    protected abstract Set<String> selectedIds();

    protected abstract void onActionCreate(xwriter x, String initStr) throws Throwable;

    protected abstract void onActionDelete(xwriter x) throws Throwable;

    protected abstract void onAction(xwriter x, Action act) throws Throwable;

}
