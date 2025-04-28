//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import b.a;

public abstract class Elem extends a implements Titled {

    private final static long serialVersionUID = 1;

    /**
     * List of identifiers to be able to navigate to the parent object in context.
     */
    private final List<String> idPath;

    /**
     * @param idPath a list of strings representing the object identifiers to
     *               navigate an object hierarchy
     */
    public Elem(final List<String> idPath) {
        this.idPath = idPath;
    }

    public final List<String> getIdPath() {
        if (idPath == null) {
            return Collections.<String>emptyList();
        }
        return idPath;
    }

    /**
     * Extends the id path with a new id.
     *
     * @param id new id to add to the copy of the path
     * @return a new id path
     */
    public final List<String> extendIdPath(final String id) {
        final ArrayList<String> ls = new ArrayList<String>();
        if (idPath != null) {
            ls.addAll(idPath);
        }
        ls.add(id);
        return ls;
    }

    /** Convenience for an integer id. */
    public final List<String> extendIdPath(final int id) {
        return extendIdPath(Integer.toString(id));
    }

}
