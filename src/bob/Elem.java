package bob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import b.a;

public abstract class Elem extends a implements Titled {
	static final long serialVersionUID = 2;

	final private List<String> idPath; // a list of identifiers to be able to navigate to the parent object in context

	/** @param idPath a list of strings representing the object identifiers to navigate an object hierarchy. */
	public Elem(final List<String> idPath) {
		this.idPath = idPath;
	}

	final public List<String> getIdPath() {
		if (idPath == null)
			return Collections.<String>emptyList();
		return idPath;
	}

	/** Extends the id path with id by copying the id path and adding the id to the list. */
	final public List<String> makeExtendedIdPath(final String id) {
		final ArrayList<String> ls = new ArrayList<String>();
		if (idPath != null) {
			ls.addAll(idPath);
		}
		ls.add(id);
		return ls;
	}

	/** Convenience for the int id. */
	final public List<String> makeExtendedIdPath(final int id) {
		return makeExtendedIdPath(Integer.toString(id));
	}
}
