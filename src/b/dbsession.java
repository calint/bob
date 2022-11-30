package b;

import java.util.List;

import db.DbObject;
import db.Query;
import db.RelAggN;
import db.test.FldChars;

public class dbsession extends DbObject {
	public final static FldChars sessionId = new FldChars(32, "");
	public final static RelAggN paths = new RelAggN(dbpathelem.class);

	public String getSessionId() {
		return getStr(sessionId);
	}

	public void setSessionId(String v) {
		set(sessionId, v);
	}

	public dbpathelem getPath(final String p) {
		final Query q = new Query(dbpathelem.path, Query.EQ, p);
		final List<DbObject> ls = paths.get(this, q, null, null);
		if (ls.isEmpty()) {
			final dbpathelem e = (dbpathelem) paths.create(this);
			e.setPath(p);
			return e;
		}
		if (ls.size() > 1) {
			b.pl(b.stacktrace(
					new RuntimeException("found more than one object for path '" + p + "' in session " + this)));
		}
		final dbpathelem e = (dbpathelem) ls.get(0);
		return e;
	}
}
