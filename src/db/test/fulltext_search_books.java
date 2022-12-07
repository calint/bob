package db.test;

import java.util.List;

import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.Limit;
import db.Query;

public class fulltext_search_books extends TestCase {
	@Override
	protected boolean isRunWithoutCache() {
		return false;
	}

	@Override
	public void doRun() throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final int nreq = 100;
		int i = 0;
		final String qstr = "+whispers +spinning +haddon";
		final Query qry = new Query(DataText.ft, qstr).and(Book.data);
		final Limit lmt = new Limit(0, 20);
		final int totalcount = tn.getCount(Book.class, null);
		Db.log("  searchable books: " + totalcount);
		while (true) {
			Db.log("   searching '" + qstr + "'");
			final int count = tn.getCount(Book.class, qry);
			Db.log("      found " + count);
			final List<DbObject> ls = tn.get(Book.class, qry, null, lmt);
			for (final DbObject o : ls) {
				final Book bo = (Book) o;
				Db.log(bo.id() + ": " + bo.getName());
			}
			Db.log("  objects retrieved: " + ls.size());
			i++;
			Db.log("requests: " + i);
			if (i == nreq)
				break;
		}
	}
}