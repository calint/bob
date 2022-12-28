package db.test;

import java.sql.Timestamp;

import db.DbObject;
import db.DbObjects;
import db.FldDateTime;
import db.FldStr;
import db.IndexRel;
import db.RelAgg;
import db.RelRef;
import db.RelRefN;

public final class Book extends DbObject {
	private static final long serialVersionUID = 1L;

	public final static FldStr name = new FldStr(800);
	public final static FldStr authors = new FldStr(3000);
	public final static FldStr publisherStr = new FldStr(400);
	public final static RelRef publisher = new RelRef(Publisher.class);
	public final static FldDateTime publishedDate = new FldDateTime();
	public final static RelAgg data = new RelAgg(DataText.class);
	public final static FldStr categoriesStr = new FldStr(800);
	public final static RelRefN categories = new RelRefN(Category.class);

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
	public String getPublisherStr() {
		return getStr(publisherStr);
	}

	public void setPublisherStr(final String v) {
		set(publisherStr, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public Timestamp getPublishedDate() {
		return getTs(publishedDate);
	}

	public void setPublishedDate(final Timestamp v) {
		set(publishedDate, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public String getCategoriesStr() {
		return getStr(categoriesStr);
	}

	public void setCategoriesStr(final String v) {
		set(categoriesStr, v);
	}

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public int getPublisherId() {
		return publisher.getId(this);
	}

	public Publisher getPublisher() {
		return (Publisher) publisher.get(this);
	}

	public void setPublisher(final int id) {
		publisher.set(this, id);
	}

	public void setPublisher(final Publisher o) {
		publisher.set(this, o.id());
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

	// ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
	public void addCategory(final int id) {
		categories.add(this, id);
	}

	public void addCategory(final Category o) {
		categories.add(this, o.id());
	}

	public DbObjects getCategories() {
		return categories.get(this);
	}

	public void removeCategory(final int id) {
		categories.remove(this, id);
	}

	public void removeCategory(final Category o) {
		categories.remove(this, o.id());
	}
}
