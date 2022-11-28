package db.test;

import java.sql.Timestamp;
import java.util.List;

import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.Query;

public class test1 extends TestCase {
	@Override
	protected boolean isResetDatabase() {
		return true;
	}

	@Override
	public void doRun() throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final User u1 = (User) tn.create(User.class);
		u1.setName("user name");
		tn.create(User.class);
		tn.create(User.class);
		int n;
		n = tn.getCount(User.class, new Query(User.name, Query.EQ, "user name"));
		if (n != 1)
			throw new RuntimeException("expected 1. got " + n);

		n = tn.getCount(User.class, null);
		if (n != 3)
			throw new RuntimeException("expected 3" + " got " + n);

		final File f1 = u1.createFile();
		final File f2 = u1.createFile();
		n = u1.getFilesCount(null);
		if (n != 2)
			throw new RuntimeException("expected 2" + " got " + n);

		f1.setName("user file");
		n = tn.getCount(File.class, new Query(File.name, Query.EQ, "user file"));
		if (n != 1)
			throw new RuntimeException("expected 1. got " + n);

		final Query q = new Query(User.name, Query.EQ, "user name").and(User.files).and(File.name, Query.EQ,
				"user file");
		n = tn.getCount(File.class, q);
		if (n != 1)
			throw new RuntimeException("expected 1. got " + n);

		tn.delete(f1);
		n = u1.getFilesCount(null);
		if (n != 1)
			throw new RuntimeException("expected 2 got " + n);

		u1.deleteFile(f2.id());
		n = u1.getFilesCount(null);
		if (n != 0)
			throw new RuntimeException("expected 0 got " + n);

		final File f3 = (File) tn.create(File.class);
		n = u1.getRefFilesCount(null);
		if (n != 0)
			throw new RuntimeException("expected 0 got " + n);

		u1.addRefFile(f3.id());
		n = u1.getRefFilesCount(null);
		if (n != 1)
			throw new RuntimeException("expected 1 got " + n);

		tn.delete(f3);
		n = u1.getRefFilesCount(null);
		if (n != 0)
			throw new RuntimeException("expected 0 got " + n);

		final File ref = u1.getGroupPic();
		if (ref != null)
			throw new RuntimeException("expected null got " + ref);

		final File f4 = (File) tn.create(File.class);
		u1.setGroupPic(f4.id());
		final File f5 = u1.getGroupPic();
		if (tn.cache_enabled && f5 != f4)
			throw new RuntimeException("expected same instance. is cache off? ");
		if (!tn.cache_enabled && f5 == f4)
			throw new RuntimeException("expected different instances. is cache on?");

		u1.setGroupPic(0);
		final File f6 = u1.getGroupPic();
		if (f6 != null)
			throw new RuntimeException("expected null");

		// test update referring table column to null at delete
		if (Db.instance().enable_update_referring_tables) {
			u1.setGroupPic(f4);
			tn.delete(f4);
			tn.commit();
			final User u2 = (User) tn.get(User.class, new Query(User.class, u1.id()), null, null).get(0);
			if (u2.getGroupPicId() != 0)
				throw new RuntimeException("expected null");
		}
//		u1.setGroupPic(0);

		final Book b1 = (Book) tn.create(Book.class);
		final DataText d = b1.getData(false);
		if (d != null)
			throw new RuntimeException("expected null got " + d);

		final DataText dt1 = b1.getData(true);
		dt1.setData("book data fulltext indexed");

		tn.commit(); // mysql does fulltext index after commit

		final List<DbObject> ls1 = tn.get(DataText.class, new Query(DataText.ft, "+fulltext +indexed"), null, null);
		if (ls1.size() != 1)
			throw new RuntimeException("expected 1 results got " + ls1.size());

		final Query q1 = new Query(DataText.ft, "+fulltext -indexed").and(Book.data).and(Book.class, b1.id());
		final List<DbObject> ls2 = tn.get(DataText.class, q1, null, null);
		if (ls2.size() != 0)
			throw new RuntimeException("expected 0 results got " + ls1.size());

		final File f7 = (File) tn.create(File.class);
		final DataBinary bin1 = f7.getData(true);
		final byte[] ba1 = new byte[] { 1, 2, 3, 4, 5 };
		bin1.setData(ba1);

		if (tn.cache_enabled)
			tn.commit(); // flush cache

		final DataBinary bin2 = f7.getData(true);
//		bin1.setData(null); // ? if not set JVM sometimes uses same instance and test fails ...
		if (bin1 == bin2) {
			// note: it seems that JVM reuses instances and bin1 == bin2 might be true
			if (bin1.getData() == bin2.getData()) {
				throw new RuntimeException("didn't expect this");
			}
		}

		final byte[] ba2 = bin2.getData();
		if (ba1.length != ba2.length)
			throw new RuntimeException("expected same length on the arrays");

		for (int i = 0; i < ba1.length; i++) {
			if (ba1[i] != ba2[i]) {
				throw new RuntimeException("gotten byte array does not match set array");
			}
		}

		// test min max

		final User u4 = (User) tn.create(User.class);
		final int u4id = u4.id();
		u4.setName(null);
		u4.setBool(false);
		u4.setNlogins(Integer.MIN_VALUE);
		u4.setLng(Long.MIN_VALUE);
		u4.setFlt(Float.MIN_VALUE);
		u4.setDbl(Double.MIN_VALUE);
//		final Timestamp ts1 = Timestamp.valueOf("1970-01-01 00:00:01"); // ? mysql cannot be committed
//		u4.setBirthTime(ts1);

		tn.commit(); // flush cache to retrieve the user from database
		final List<DbObject> ls = tn.get(User.class, new Query(User.class, u4id), null, null);
		if (ls.size() != 1)
			throw new RuntimeException("expected to find one user with id " + u4id);
		final User u5 = (User) ls.get(0);
		if (u5.getName() != null)
			throw new RuntimeException();
		if (u5.isBool() != false)
			throw new RuntimeException();
		if (u5.getNlogins() != Integer.MIN_VALUE)
			throw new RuntimeException();
		if (u5.getLng() != Long.MIN_VALUE)
			throw new RuntimeException();
		if (u5.getFlt() != Float.MIN_VALUE)
			throw new RuntimeException();
		if (u5.getDbl() != Double.MIN_VALUE)
			throw new RuntimeException();
//		if (!u5.getBirthTime().equals(ts1))
//			throw new RuntimeException();

		final String s1 = "testing string \0 \' \" \r \n \\ ᐖᐛツ";
		u4.setName(s1);
		u4.setBool(true);
		u4.setNlogins(Integer.MAX_VALUE);
		u4.setLng(Long.MAX_VALUE);
//		u4.setFlt(Float.MAX_VALUE); // ? mysql problems with float
//		u4.setFlt(3.402823466E+38f); // ? mysql max is 3.402823466E+38 but it does not work
		u4.setDbl(Double.MAX_VALUE);
		final Timestamp ts2 = Timestamp.valueOf("2038-01-19 03:14:07");
		u4.setBirthTime(ts2);

		tn.commit(); // flush cache to retrieve the user from database

		final List<DbObject> ls3 = tn.get(User.class, new Query(User.class, u4id), null, null);
		if (ls3.size() != 1)
			throw new RuntimeException("expected to find one user with id " + u4id);
		final User u6 = (User) ls3.get(0);

		if (!s1.equals(u6.getName()))
			throw new RuntimeException();
		if (u6.isBool() != true)
			throw new RuntimeException();
		if (u6.getNlogins() != Integer.MAX_VALUE)
			throw new RuntimeException();
		if (u6.getLng() != Long.MAX_VALUE)
			throw new RuntimeException();
//		if (u6.getFlt() != Float.MAX_VALUE) // ? mysql problems with float
//			throw new RuntimeException();
		if (u6.getDbl() != Double.MAX_VALUE)
			throw new RuntimeException();
		if (!u6.getBirthTime().equals(ts2))
			throw new RuntimeException();

		u4.setName(null);
		u4.setFlt(1.2f);
		u4.setDbl(1.2);
		tn.commit();
		final List<DbObject> ls4 = tn.get(User.class, new Query(User.class, u4id), null, null);
		if (ls4.size() != 1)
			throw new RuntimeException("expected to find one user with id " + u4id);
		final User u7 = (User) ls4.get(0);
		if (u7.getFlt() != 1.2f)
			throw new RuntimeException();
		if (u7.getDbl() != 1.2)
			throw new RuntimeException();

		final List<DbObject> ls5 = tn.get(File.class, null, null, null);
		for (final DbObject o : ls5) {
			tn.delete(o);
		}

		u4.createFile(); // create a file to cascade delete for user 4

		final List<DbObject> ls6 = tn.get(User.class, null, null, null);
		for (final DbObject o : ls6) {
			tn.delete(o);
		}

		final List<DbObject> ls7 = tn.get(User.class, null, null, null);
		if (!ls7.isEmpty())
			throw new RuntimeException();

		final List<DbObject> ls8 = tn.get(File.class, null, null, null);
		if (!ls8.isEmpty())
			throw new RuntimeException();

		final List<DbObject> ls9 = tn.get(Book.class, null, null, null);
		for (final DbObject o : ls9) {
			tn.delete(o);
		}

		final User u8 = (User) tn.create(User.class);
		final File f8 = u8.createFile(); // AggN
		f8.getData(true);
		u8.createGame();
		u8.createGame();
		tn.delete(u8); // d1, g1, g2 gets deleted with "delete from" instead of get delete because they
						// don't not aggregate

		final User u9 = (User) tn.create(User.class);
		final File f9 = (File) tn.create(File.class);
		f9.setName("dog ok");
		f9.loadFile("img/far_side_dog_ok.jpg");
		u9.setGroupPic(f9);
		tn.commit();
		final File f10 = u9.getGroupPic();
		f10.writeFile("img/dog_ok.jpg");

		// ! on unix only
		final int procres = Runtime.getRuntime().exec("diff img/far_side_dog_ok.jpg img/dog_ok.jpg").waitFor();
		if (procres != 0)
			throw new RuntimeException();
		if (!new java.io.File("img/dog_ok.jpg").delete())
			throw new RuntimeException();

		tn.delete(f10);
		final File f11 = u9.getGroupPic(); // ! has dangling reference
		if (f11 != null)
			throw new RuntimeException();

		// bugfix: user created committed (cache cleared) then deleted where delete
		// tries to remove it from the cache
		final User u10 = (User) tn.create(User.class);
		u10.setName("John Doe");
		tn.commit();
		tn.delete(u10);

	}
}