package db.test;

import java.util.List;

import db.Db;
import db.DbObject;
import db.DbTransaction;

// 	delete all books (cascading delete of aggregated DataText)
public class delete_books extends TestCase {
	@Override
	public void doRun() {
		final DbTransaction tn = Db.currentTransaction();
		List<DbObject> ls = tn.get(Book.class, null, null, null);
		for (final DbObject o : ls) {
			tn.delete(o);
		}
	}
}