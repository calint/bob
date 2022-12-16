package bob;

import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;

public abstract class view extends a implements titled {
	static final long serialVersionUID = 1;

	protected abstract List<action> getActionsList();

	protected abstract List<?> getList();

	protected abstract String getIdFrom(Object o);

	protected abstract String getNameFrom(Object o);

	protected abstract void onDelete(xwriter x, Set<String> selectedIds) throws Throwable;
}
