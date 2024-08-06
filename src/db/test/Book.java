package db.test;

import java.sql.Timestamp;

import bob.Titled;
import db.DbObject;
import db.DbObjects;
import db.FldBool;
import db.FldDateTime;
import db.FldFlt;
import db.FldInt;
import db.FldStr;
import db.IndexRel;
import db.RelAgg;
import db.RelRef;
import db.RelRefN;

public final class Book extends DbObject implements Titled {
    public final static FldStr name = new FldStr(800);
    public final static FldStr authorsStr = new FldStr(3000);
    public final static RelRefN authors = new RelRefN(Author.class);
    public final static FldStr publisherStr = new FldStr(400);
    public final static RelRef publisher = new RelRef(Publisher.class);
    public final static FldDateTime publishedDate = new FldDateTime();
    public final static RelAgg data = new RelAgg(DataText.class);
    public final static FldStr categoriesStr = new FldStr(800);
    public final static RelRefN categories = new RelRefN(Category.class);
    public final static FldInt inStock = new FldInt();
    public final static FldBool showInStore = new FldBool();
    public final static FldFlt rating = new FldFlt();

    // optimizes Book join with DataText when doing full text query
    public final static IndexRel ixRelData = new IndexRel(data);

    public String getTitle() {
        return getName();
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getName() {
        return name.getStr(this);
    }

    public void setName(final String v) {
        name.setStr(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getAuthorsStr() {
        return authorsStr.getStr(this);
    }

    public void setAuthorsStr(final String v) {
        authorsStr.setStr(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getPublisherStr() {
        return publisherStr.getStr(this);
    }

    public void setPublisherStr(final String v) {
        publisherStr.setStr(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public Timestamp getPublishedDate() {
        return publishedDate.getDateTime(this);
    }

    public void setPublishedDate(final Timestamp v) {
        publishedDate.setDateTime(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public String getCategoriesStr() {
        return categoriesStr.getStr(this);
    }

    public void setCategoriesStr(final String v) {
        categoriesStr.setStr(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public int getInStock() {
        return inStock.getInt(this);
    }

    public void setInStock(final int v) {
        inStock.setInt(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public boolean isShowInStore() {
        return showInStore.getBool(this);
    }

    public void setShowInStore(final boolean v) {
        showInStore.setBool(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public float getRating() {
        return rating.getFlt(this);
    }

    public void setRating(final float v) {
        rating.setFlt(this, v);
    }

    // ---- - - - - - ---- -- --- - -- - -- - -- -- - -- - - - -- - - --- - -
    public void addAuthor(final int id) {
        authors.add(this, id);
    }

    public void addAuthor(final Author o) {
        authors.add(this, o.id());
    }

    public DbObjects getAuthors() {
        return authors.get(this);
    }

    public void removeAuthor(final int id) {
        authors.remove(this, id);
    }

    public void removeAuthor(final Author o) {
        authors.remove(this, o.id());
    }

    public void removeAllAuthors() {
        authors.removeAll(this);
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
        publisher.set(this, o == null ? 0 : o.id());
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

    public void removeAllCategories() {
        categories.removeAll(this);
    }
}
