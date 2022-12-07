package db.test;

import java.sql.Timestamp;

import db.DbObject;
import db.FldStr;
import db.FldTs;
import db.IndexRel;
import db.RelAgg;

public final class Book extends DbObject {
	public final static FldStr name = new FldStr(800);
	public final static FldStr authors = new FldStr(3000);
	public final static FldStr publisher = new FldStr(400);
	public final static FldTs publishedDate = new FldTs();
	public final static RelAgg data = new RelAgg(DataText.class);

	// optimizes Book join with DataText when doing full text query
	public final static IndexRel ixRelData = new IndexRel(data);

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getName() {
		return getStr(name);
	}

	public void setName(final String v) {
		set(name, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getAuthors() {
		return getStr(authors);
	}

	public void setAuthors(final String v) {
		set(authors, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getPublisher() {
		return getStr(publisher);
	}

	public void setPublisher(final String v) {
		set(publisher, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public Timestamp getPublishedDate() {
		return getTs(publishedDate);
	}

	public void setPublishedDate(final Timestamp v) {
		set(publishedDate, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public int getDataId() {
		return data.getId(this);
	}

	public DataText getData(final boolean createIfNone) {
		return (DataText) data.get(this, createIfNone);
	}

	public void deleteData() {
		data.delete(this);
	}

}