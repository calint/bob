package db.test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import db.Db;
import db.DbTransaction;
import db.Query;

public class test2 extends TestCase {
	@Override
	public void doRun() throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add("hello");
		ls.add("world");
		final String chs = "12345678901234567890123456789012";
		final TestObj to = (TestObj) tn.create(TestObj.class);
		final Query qid = new Query(TestObj.class, to.id());
		to.setMd5(chs);
		to.setList(ls);
		tn.commit();
		final TestObj to2 = (TestObj) tn.get(TestObj.class, qid, null, null).get(0);
		final List<String> ls2 = to2.getList();
		if (ls.size() != ls2.size())
			throw new RuntimeException();
		for (int i = 0; i < ls2.size(); i++) {
			if (!ls.get(i).equals(ls2.get(i)))
				throw new RuntimeException();
		}
		final String s = to.getMd5();
		if (!chs.equals(s))
			throw new RuntimeException();
//		ls2.add("!");
//		tn.commit();
//		final TestObj to8 = (TestObj) tn.get(TestObj.class, qid, null, null).get(0);
//		final List<String> ls3 = to8.getList();
//		ls.add("!");
//		if (ls.size() != ls3.size())
//			throw new RuntimeException();
//		for (int i = 0; i < ls3.size(); i++) {
//			if (!ls.get(i).equals(ls3.get(i)))
//				throw new RuntimeException();
//		}
				
		final TestObj to3 = (TestObj) tn.get(TestObj.class, qid, null, null).get(0);
		to3.setList(null);
		if (to3.getList() != null)
			throw new RuntimeException();
		Db.currentTransaction().commit();
		final TestObj to4 = (TestObj) tn.get(TestObj.class, qid, null, null).get(0);
		if (to4.getList() != null)
			throw new RuntimeException();

		final Timestamp ts = Timestamp.valueOf("2022-11-26 14:07:00");
		to4.setDateTime(ts);
		tn.commit();
		final TestObj to5 = (TestObj) tn.get(TestObj.class, qid, null, null).get(0);
		if (!to5.getDateTime().equals(ts))
			throw new RuntimeException();

		// min value from https://dev.mysql.com/doc/refman/8.0/en/datetime.html
//		final Timestamp ts2 = Timestamp.valueOf("1000-01-01 00:00:00.000000");

		final Timestamp ts2 = Timestamp.valueOf("0001-01-01 00:00:00.000000");
		to4.setDateTime(ts2);
		tn.commit();
		final TestObj to6 = (TestObj) tn.get(TestObj.class, qid, null, null).get(0);
		if (!to6.getDateTime().equals(ts2))
			throw new RuntimeException();

		// max value from https://dev.mysql.com/doc/refman/8.0/en/datetime.html
//		final Timestamp ts3 = Timestamp.valueOf("9999-12-31 23:59:59.999999"); // overflows

		final Timestamp ts3 = Timestamp.valueOf("9999-12-31 23:59:59");
		to4.setDateTime(ts3);
		tn.commit();
		final TestObj to7 = (TestObj) tn.get(TestObj.class, qid, null, null).get(0);
		if (!to7.getDateTime().equals(ts3))
			throw new RuntimeException();

//		final Timestamp ts4 = Timestamp.valueOf("-0001-12-31 23:59:59");
//		to4.setDateTime(ts4);
//		tn.commit();
//		final TestObj to8 = (TestObj) tn.get(TestObj.class, qid, null, null).get(0);
//		if (!to8.getDateTime().equals(ts4))
//			throw new RuntimeException();

		tn.delete(to7);
	}
}