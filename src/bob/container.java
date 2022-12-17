package bob;

import java.util.ArrayList;
import java.util.List;

import b.a;
import b.xwriter;

/**
 * Contains elements in a list. Used to create a name space for dynamically
 * created elements.
 */
public final class container extends a {
	private static final long serialVersionUID = 1L;
	private final ArrayList<a> elements = new ArrayList<a>();

	@Override
	public void to(final xwriter x) throws Throwable {
		final int n = elements.size();
		final int le = n - 1;
		for (int i = 0; i < n; i++) {
			final a e = elements.get(i);
			e.to(x);
			if (i != le) {
				x.p(" • ");
			}
		}
	}

	public void add(final a e) {
		e.parent(this);
		e.name(Integer.toString(elements.size()));
		elements.add(e);
	}

	/** @return read-only list of elements. To add element use add(...). */
	public List<a> elements() {
		return elements;
	}

	@Override
	public a child(final String nm) {
		final a e = super.child(nm);
		if (e != null)
			return e;
		return elements.get(Integer.parseInt(nm));
	}
}
