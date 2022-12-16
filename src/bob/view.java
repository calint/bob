package bob;

import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;

public abstract class view extends a implements titled {
	static final long serialVersionUID = 1;

	protected abstract List<action> getActionsList();

	protected abstract List<?> getObjectsList();

	protected abstract String getIdFrom(Object o);

	protected abstract String getNameFrom(Object o);

	protected abstract Set<String> getSelectedIds();

	protected abstract void onActionCreate(xwriter x, String init_str) throws Throwable;

	protected abstract void onActionDelete(xwriter x) throws Throwable;

	protected abstract void onAction(xwriter x, action act) throws Throwable;
}
