package db.test;

import java.util.List;

import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.Query;

public class test3 extends TestCase {
    public test3() {
	this(false);
    }

    public test3(final boolean use_current_transaction) {
	super(use_current_transaction);
    }

    @Override
    protected boolean isResetDatabase() {
	return true;
    }

    @Override
    public void doRun() throws Throwable {
	final DbTransaction tn = Db.currentTransaction();
	final User u = (User) tn.create(User.class);
	final File f1 = u.createFile();
	f1.setName("file 1");
	final File f2 = u.createFile();
	f2.setName("file 2");
	final File f3 = (File) tn.create(File.class);

	final Query q = new Query(User.files);
	final List<DbObject[]> ls = tn.get(new Class<?>[] { User.class, File.class }, q, null, null);
	if (ls.size() != 2)
	    throw new RuntimeException();
	if (tn.cache_enabled) {
	    DbObject[] row;
	    row = ls.get(0);
	    if (row[0] != u)
		throw new RuntimeException();
	    if (row[1] != f1)
		throw new RuntimeException();

	    row = ls.get(1);
	    if (row[0] != u)
		throw new RuntimeException();
	    if (row[1] != f2)
		throw new RuntimeException();
	} else {
	    DbObject[] row;
	    row = ls.get(0);
	    if (row[0].id() != u.id())
		throw new RuntimeException();
	    if (row[1].id() != f1.id())
		throw new RuntimeException();

	    row = ls.get(1);
	    if (row[0].id() != u.id())
		throw new RuntimeException();
	    if (row[1].id() != f2.id())
		throw new RuntimeException();
	}

	tn.delete(u);
	tn.delete(f3);
    }
}
