package bob;

import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;

public abstract class view extends a implements titled {
	static final long serialVersionUID = 1;

	public final static int BIT_CREATE = 1;
	public final static int BIT_DELETE = 2;
	public final static int BIT_SEARCH = 4;
	public final static int BIT_SELECT = 8;
	/** The actions that are enabled in the view. */
	protected int enabled_view_bits;

	public view(int enabled_bits) {
		this.enabled_view_bits = enabled_bits;
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

	protected List<action> getActionsList() {
		return null;
	}

	protected abstract List<?> getObjectsList();

	protected abstract String getIdFrom(Object o);

	protected abstract String getNameFrom(Object o);

	protected abstract Set<String> getSelectedIds();

	protected abstract void onActionCreate(xwriter x, String init_str) throws Throwable;

	protected abstract void onActionDelete(xwriter x) throws Throwable;

	protected abstract void onAction(xwriter x, action act) throws Throwable;
}
