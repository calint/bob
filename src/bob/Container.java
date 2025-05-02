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
public final class Container extends a {

    private final static long serialVersionUID = 1;

    private ArrayList<a> elements;

    @Override
    public void to(final xwriter x) throws Throwable {
        if (elements == null) {
            return;
        }
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
        if (elements == null) {
            elements = new ArrayList<a>();
        }
        e.name(Integer.toString(elements.size()));
        elements.add(e);
    }

    public boolean isEmpty() {
        if (elements == null) {
            return true;
        }
        return elements.isEmpty();
    }

    public void clear() {
        if (elements == null) {
            return;
        }
        elements.clear();
    }

    @Override
    public a child(final String nm) {
        final a e = super.child(nm);
        if (e != null) {
            return e;
        }
        if (elements == null) {
            return null;
        }
        return elements.get(Integer.parseInt(nm));
    }

}
