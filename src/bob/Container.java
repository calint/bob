// reviewed: 2024-08-05
package bob;

import java.util.ArrayList;

import b.a;
import b.xwriter;

/**
 * Contains elements in a list. Used to create a name space for dynamically
 * created elements.
 */
public final class Container extends a {
    private static final long serialVersionUID = 1;

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

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public void clear() {
        elements.clear();
    }

    @Override
    public a child(final String nm) {
        final a e = super.child(nm);
        if (e != null) {
            return e;
        }
        return elements.get(Integer.parseInt(nm));
    }

}
