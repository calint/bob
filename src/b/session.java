package b;

import java.util.List;

import db.DbObject;
import db.Index;
import db.Query;
import db.RelAggN;
import db.test.FldChars;

public final class session extends DbObject {
	public final static FldChars sessionId = new FldChars(32, "");
	public final static RelAggN paths = new RelAggN(sessionpath.class);
	public final static Index ixSessionId=new Index(sessionId);

	public String session_id() {
		return getStr(sessionId);
	}

	public void session_id(String v) {
		set(sessionId, v);
	}

	public sessionpath path(final String p) {
		final Query q = new Query(sessionpath.path, Query.EQ, p);
		final List<DbObject> ls = paths.get(this, q, null, null);
		if (ls.isEmpty()) {
			final sessionpath e = (sessionpath) paths.create(this);
			e.path(p);
			return e;
		}
		if (ls.size() > 1) {
			b.pl(b.stacktrace(
					new RuntimeException("found more than one object for path '" + p + "' in session " + this)));
		}
		final sessionpath e = (sessionpath) ls.get(0);
		return e;
	}
}
