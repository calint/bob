package bob;

import java.util.List;
import java.util.Set;

import b.a;
import b.xwriter;

public abstract class view extends a implements titled {
	static final long serialVersionUID = 1;

	protected abstract List<action> getActionsList();

	protected abstract void onCreate(xwriter x, String init_str) throws Throwable;

	protected abstract List<?> getObjectsList();

	protected abstract String getIdFrom(Object o);

	protected abstract String getNameFrom(Object o);

	protected abstract Set<String> getSelectedIds();

	protected abstract void onDelete(xwriter x) throws Throwable;
}
