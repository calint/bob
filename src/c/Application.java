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
//		Db.enable_log = false;
		Db.log(getClass().getName() + ": init");
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
		db.init("jdbc:mysql://" + b.bapp_jdbc_host + "/" + b.bapp_jdbc_db + "?allowPublicKeyRetrieval=true&useSSL=false",
				b.bapp_jdbc_user, b.bapp_jdbc_password, Integer.parseInt(b.bapp_jdbc_ncons));
	}

	@Override
	public void shutdown() throws Throwable {
		Db.log(getClass().getName() + ": shutdown");
		Db.instance().shutdown();
	}
}
