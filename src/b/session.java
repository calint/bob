// reviewed: 2024-08-05
package b;

import java.sql.Timestamp;
import java.util.List;

import db.DbObject;
import db.FldChars;
import db.FldTs;
import db.Index;
import db.Query;
import db.RelAggN;

/** Persistent session. */
public final class session extends DbObject {
    public final static FldChars sessionId = new FldChars(32, "");
    public final static FldTs createdTs = new FldTs();
    public final static RelAggN objects = new RelAggN(sessionobj.class);
    public final static Index ixSessionId = new Index(sessionId);

    @Override
    protected void onCreate() {
        createdTs.setTs(this, new Timestamp(System.currentTimeMillis()));
    }

    public String session_id() {
        return sessionId.getChars(this);
    }

    public void session_id(final String v) {
        sessionId.setChars(this, v);
    }

    public Timestamp createdTs() {
        return createdTs.getTs(this);
    }

    public sessionobj object(final String path) {
        final Query q = new Query(sessionobj.path, Query.EQ, path);
        final List<DbObject> ls = objects.get(this).get(q).toList();
        if (ls.isEmpty()) {
            final sessionobj e = (sessionobj) objects.create(this);
            e.path(path);
            return e;
        }
        if (ls.size() > 1) {
            b.pl(b.stacktrace(
                    new RuntimeException("found more than one object for path '" + path + "' in session " + this)));
        }
        return (sessionobj) ls.get(0);
    }
}
