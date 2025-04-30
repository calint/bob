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

    private final IdPath idPath;

    /**
     * @param idPath Path to parent of element in context or null if none.
     */
    public Elem(final IdPath idPath) {
        this.idPath = idPath;
    }

    /** @return Path to parent of element in context or null if none. */
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
        return IdPath.extend(idPath, id);
    }

    /** Convenience for an integer id. */
    public final IdPath extendIdPath(final int id) {
        return extendIdPath(Integer.toString(id));
    }

    public final static class IdPath implements Serializable {

        private final static long serialVersionUID = 1;

        private ArrayList<String> ids;

        /**
         * @param idPth `IdPath` to extend. May be null.
         * @return New instance extended by id.
         */
        public static IdPath extend(final IdPath idPth, final String id) {
            final IdPath pth = new IdPath();
            if (idPth != null) {
                pth.add(idPth);
            }
            pth.add(id);
            return pth;
        }

        /** Convenience for int id converting it to String. */
        public static IdPath extend(final IdPath idPth, final int id) {
            return IdPath.extend(idPth, Integer.toString(id));
        }

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

        protected void add(final IdPath pth) {
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            if (pth == null) {
                return;
            }
            ids.addAll(pth.ids);
        }

    }

}
