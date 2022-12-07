package db.test;

import java.util.List;

import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.Limit;

public class get_books extends TestCase {
	@Override
	protected boolean isRunWithoutCache() {
		return false;
	}

	@Override
	public void doRun() throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final int nreq = 100;
		int i = 0;
		while (true) {
			final List<DbObject> ls = tn.get(Book.class, null, null, new Limit(0, 1000000));
//				for (final DbObject o : ls) {
//					final Book bo = (Book) o;
//					System.out.println(bo.getName());
//				}
			System.out.println("objects retrieved: " + ls.size());
			i++;
			System.out.println("requests: " + i);
			if (i == nreq)
				break;
		}
	}
}