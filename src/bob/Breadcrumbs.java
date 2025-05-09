//
// reviewed: 2024-08-05
//           2025-04-28
//           2025-05-02
//
package bob;

import java.util.ArrayList;

import b.a;
import b.xwriter;

/**
 * Contains elements in a list. Used to create a name space for dynamically
 * created elements.
 */
public final class Breadcrumbs extends a {

    private final static long serialVersionUID = 1;

    private final ArrayList<a> elements = new ArrayList<a>();

    @Override
    public void to(final xwriter x) {
        final int n = elements.size();
        final int lastElemIx = n - 1;
        for (int i = 0; i < n; i++) {
            final a e = elements.get(i);
            x.p(" &raquo; ");
            final String nm;
            if (e instanceof Titled) { // ? oop?
                nm = ((Titled) e).title();
            } else { // ? oop
                nm = e.getClass().getName();
            }
            if (i == lastElemIx) {
                x.tag("em").p(nm).tage("em");
            } else {
                x.ax(this, "clk " + i, nm);
            }
        }
    }

    public void x_clk(final xwriter x, final String param) throws Throwable {
        final int i = Integer.parseInt(param);
        final int n = elements.size();
        for (int j = n - 1; j > i; j--) {
            elements.remove(j);
        }
        x.xu(this);
        bubble_event(x);
    }

    public void add(final a e) {
        elements.add(e);
    }

    public a active() {
        if (elements.isEmpty()) {
            return null;
        }
        return elements.get(elements.size() - 1);
    }

    @Override
    public a child(final String nm) {
        final a e = super.child(nm);
        if (e != null) {
            return e;
        }
        return elements.get(Integer.parseInt(nm));
    }

    public void pop() {
        elements.remove(elements.size() - 1);
    }

    public void clear() {
        elements.clear();
    }

    public int elementCount() {
        return elements.size();
    }
}
