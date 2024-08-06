// reviewed: 2024-08-05
package bob;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import b.xwriter;

public abstract class View extends Elem {
    private static final long serialVersionUID = 1;

    public final static int BIT_CREATE = 1;
    public final static int BIT_DELETE = 2;
    public final static int BIT_SEARCH = 4;
    public final static int BIT_SELECT = 8;

    /** The actions that are enabled in the view. */
    final protected int enabledViewBits;

    /** True if view renders to select item(s) */
    private boolean isSelectMode;

    /** True if view renders for selection of multiple items. */
    private boolean isSelectModeMulti;

    /** Receiver of selection of multiple items. */
    private SelectReceiverMulti selectReceiverMulti;

    /** Receiver of selection of single item. */
    private SelectReceiverSingle selectReceiverSingle;

    /** Name and plural of the object type. */
    final private View.TypeInfo typeInfo;

    public View(final List<String> idPath, final int enabledBits, final TypeInfo ti) {
        super(idPath);
        enabledViewBits = enabledBits;
        typeInfo = ti == null ? new TypeInfo("object", "objects") : ti;
    }

    final public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    final public boolean isSelectMode() {
        return isSelectMode;
    }

    final public boolean isSelectModeMulti() {
        return isSelectModeMulti;
    }

    final public SelectReceiverMulti getSelectReceiverMulti() {
        return selectReceiverMulti;
    }

    final public SelectReceiverSingle getSelectReceiverSingle() {
        return selectReceiverSingle;
    }

    protected List<Action> getActionsList() {
        return null;
    }

    /**
     * Returns the object count per page.
     *
     * @return objects per page or 0 if paging is disabled.
     */
    protected abstract int getObjectsPerPageCount();

    /**
     * If paging is enabled this will be called before getObjectsList().
     * 
     * @return number of objects in the list
     */
    protected abstract int getObjectsCount();

    /** @return objects in this list */
    protected abstract List<?> getObjectsList();

    /**
     * Called to get the id from an object. Used to link views to other views or
     * forms.
     */
    protected abstract String getIdFrom(Object obj);

    protected abstract Set<String> getSelectedIds();

    protected abstract void onActionCreate(xwriter x, String initStr) throws Throwable;

    protected abstract void onActionDelete(xwriter x) throws Throwable;

    protected abstract void onAction(xwriter x, Action act) throws Throwable;

    public final static class TypeInfo implements Serializable {
        private static final long serialVersionUID = 1;

        protected final String name;
        protected final String namePlural;

        public TypeInfo(final String name, final String namePlural) {
            this.name = name;
            this.namePlural = namePlural;
        }

        public String getName() {
            return name;
        }

        public String getNamePlural() {
            return namePlural;
        }
    }

    interface SelectReceiverMulti extends Serializable {
        void onSelect(Set<String> selectedIds);
    }

    interface SelectReceiverSingle extends Serializable {
        void onSelect(String selectedId);
    }

    public final void setSelectMode(final Set<String> selectedIds, final SelectReceiverMulti sr) {
        isSelectMode = true;
        isSelectModeMulti = true;
        selectReceiverMulti = sr;
        final Set<String> selection = getSelectedIds();
        selection.clear();
        selection.addAll(selectedIds);
    }

    public final void setSelectMode(final String selectedId, final SelectReceiverSingle sr) {
        isSelectMode = true;
        isSelectModeMulti = false;
        selectReceiverSingle = sr;
    }

}
