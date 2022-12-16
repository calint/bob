package bob;

import java.util.ArrayList;

import b.a;
import b.xwriter;

/**
 * Contains elements in a list. Used to create a name space for dynamically
 * created elements.
 */
public final class bread_crumbs extends a {
	private static final long serialVersionUID = 1L;
	private ArrayList<a> elements = new ArrayList<a>();

	@Override
	public void to(xwriter x) {
		final int n = elements.size();
		final int lei = n - 1; // last element index
		for (int i = 0; i < n; i++) {
			final a e = elements.get(i);
			x.p(" &raquo; ");
			final String nm;
			if (e instanceof titled) // ? oop
				nm = ((titled) e).getTitle();
			else
				nm = getClass().getName();
			if (i == lei) {
				x.tag("em").p(nm).tage("em");
				return;
			}
			x.ax(this, "clk " + i, nm);
		}
	}

	public void x_clk(xwriter x, String param) throws Throwable {
		final int i = Integer.parseInt(param);
		final int n = elements.size();
		if (i + 1 < n) {
			for (int j = n - 1; j > i; j--) {
				elements.remove(j);
			}
		}
		x.xu(this);
		bubble_event(x);
	}

	public void add(a e) {
		elements.add(e);
	}

	public a getActive() {
		return elements.get(elements.size() - 1);
	}

	@Override
	protected a find_child(String nm) {
		return elements.get(Integer.parseInt(nm));
	}

	public void removeLast() {
		elements.remove(elements.size() - 1);
	}
}
