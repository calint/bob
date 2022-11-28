package c;

import b.b;
import b.bapp;
import db.Db;
import db.test.Book;
import db.test.DataBinary;
import db.test.DataText;
import db.test.File;
import db.test.Game;
import db.test.TestObj;
import db.test.User;

public class Application implements bapp {
	@Override
	public void init() throws Throwable {
		b.pl(getClass().getName()+": init");
		Db.initInstance();
		final Db db = Db.instance();
		db.register(User.class);
		db.register(File.class);
		db.register(DataBinary.class);
		db.register(DataText.class);
		db.register(Book.class);
		db.register(Game.class);
		db.register(TestObj.class);
//		db.init("jdbc:mysql://localhost:3306/testdb", "c", "password", 5);
		db.init("jdbc:mysql://localhost:3306/testdb?allowPublicKeyRetrieval=true&useSSL=false", "c", "password", 10);
	}
	@Override
	public void shutdown() throws Throwable {
		b.pl(getClass().getName()+": shutdown");
		Db.instance().shutdown();
	}
}
