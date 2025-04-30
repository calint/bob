//
// reviewed: 2024-08-05
//           2025-04-28
//
package bob;

import java.io.Serializable;
import java.util.ArrayList;

import b.a;

public abstract class Elem extends a implements Titled {

    private final static long serialVersionUID = 1;

    /**
     * List of identifiers up to object but not including object in context.
     */
    private final IdPath idPath;

    /**
     * @param idPath Identifiers for navigation of object hierarchy to parent
     *               object.
     */
    public Elem(final IdPath idPath) {
        this.idPath = idPath;
    }

    /** @return Path to parent of object in context or null if none. */
    public final IdPath idPath() {
        return idPath;
    }

    /**
     * Extends the id path with a new id.
     *
     * @param id New id to add to a copy of the path.
     * @return A new id path.
     */
    public final IdPath extendIdPath(final String id) {
        final IdPath pth = new IdPath();
        if (idPath != null) {
            pth.add(idPath);
        }
        pth.add(id);
        return pth;
    }

    /** Convenience for an integer id. */
    public final IdPath extendIdPath(final int id) {
        return extendIdPath(Integer.toString(id));
    }

    /** A path of ids up to but not including current element. */
    public final static class IdPath implements Serializable {

        private final static long serialVersionUID = 1;

        private ArrayList<String> ids;

        /** @return Parent object idi or null if none. */
        public String current() {
            if (ids == null) {
                return null;
            }
            return ids.get(ids.size() - 1);
        }

        public void add(final String id) {
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(id);
        }

        public String get(final int ix) {
            return ids.get(ix);
        }

        void add(final IdPath pth) {
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.addAll(pth.ids);
        }

    }

}
