package b;

import java.util.List;
import db.DbObject;
import db.Index;
import db.Query;
import db.RelAggN;
import db.test.FldChars;

public final class session extends DbObject{
	public final static FldChars sessionId=new FldChars(32,"");
	public final static RelAggN objects=new RelAggN(sessionobj.class);
	public final static Index ixSessionId=new Index(sessionId);

	public String session_id(){
		return sessionId.getChars(this);
	}

	public void session_id(final String v){
		sessionId.setChars(this,v);
	}

	public sessionobj object(final String path){
		final Query q=new Query(sessionobj.path,Query.EQ,path);
		final List<DbObject> ls=objects.get(this).get(q).toList();
		if(ls.isEmpty()){
			final sessionobj e=(sessionobj)objects.create(this);
			e.path(path);
			return e;
		}
		if(ls.size()>1){
			b.pl(b.stacktrace(new RuntimeException("found more than one object for path '"+path+"' in session "+this)));
		}
		return (sessionobj)ls.get(0);
	}
}
