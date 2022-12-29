package bob;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;

public abstract class View extends a implements Titled {
	static final long serialVersionUID = 1;

	public final static int BIT_CREATE = 1;
	public final static int BIT_DELETE = 2;
	public final static int BIT_SEARCH = 4;
	public final static int BIT_SELECT = 8;
	/** The actions that are enabled in the view. */
	final protected int enabledViewBits;

	public View(final int enabledBits) {
		enabledViewBits = enabledBits;
	}
//
//	protected final void enable(int bit) {
//		enabled_bits |= bit;
//	}
//
//	protected final void disable(int bit) {
//		enabled_bits &= ~bit;
//	}

//	protected final boolean isEnabled(int bit) {
//		return (enabled_bits & bit) == bit;
//	}

	protected List<Action> getActionsList() {
		return null;
	}

	protected abstract View.TypeInfo getTypeInfo();

	/**
	 * Returns the object count per page.
	 *
	 * @return objects per page or 0 if paging is disabled.
	 */
	protected abstract int getObjectsPerPageCount();

	/** If paging is enabled this will be called before getObjectsList(). */
	protected abstract int getObjectsCount();

	protected abstract List<?> getObjectsList();

	/**
	 * Called to get the id from an object. Used to link views to other views or
	 * forms.
	 */
	protected abstract String getIdFrom(Object o);

	protected abstract Set<String> getSelectedIds();

	protected abstract void onActionCreate(xwriter x, String initStr) throws Throwable;

	protected abstract void onActionDelete(xwriter x) throws Throwable;

	protected abstract void onAction(xwriter x, Action act) throws Throwable;

	public final static class TypeInfo implements Serializable {
		private static final long serialVersionUID = 1L;
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
}
