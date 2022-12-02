package c;

import b.bapp;
import db.Db;
import db.test.Book;
import db.test.DataBinary;
import db.test.DataText;
import db.test.File;
import db.test.Game;
import db.test.TestObj;
import db.test.User;

public class application implements bapp {
	private static application inst;

	public static application instance() {
		return inst;
	}

	public application() {
		inst = this;
	}

	/** called by b at startup */
	@Override
	public void init() throws Throwable {
//		Db.enable_log = false;
		Db.log(getClass().getName() + ": init");
		final Db db = Db.instance();
		db.register(User.class);
		db.register(File.class);
		db.register(DataBinary.class);
		db.register(DataText.class);
		db.register(Book.class);
		db.register(Game.class);
		db.register(TestObj.class);
		
		b.b.set_path_to_class("/",c.shell.class);
		b.b.set_path_to_class("/websocket",c.websocket.class);
//		db.init("jdbc:mysql://localhost:3306/testdb", "c", "password", 5);
//		db.init("jdbc:mysql://" + b.bapp_jdbc_host + "/" + b.bapp_jdbc_db
//				+ "?allowPublicKeyRetrieval=true&useSSL=false", b.bapp_jdbc_user, b.bapp_jdbc_password,
//				Integer.parseInt(b.bapp_jdbc_ncons));
//		db.init("jdbc:mysql://" + b.bapp_jdbc_host + "/" + b.bapp_jdbc_db
//				+ "?ssl-mode=REQUIRED", b.bapp_jdbc_user, b.bapp_jdbc_password,
//				Integer.parseInt(b.bapp_jdbc_ncons));
//		db.init("jdbc:mysql://" + b.bapp_jdbc_host + "/" + b.bapp_jdbc_db
//				+ "?verifyServerCertificate=false&useSSL=true&ssl-mode=REQUIRED", b.bapp_jdbc_user, b.bapp_jdbc_password,
//				Integer.parseInt(b.bapp_jdbc_ncons));
		
		
// verifyServerCertificate=false&useSSL=true&ssl-mode=REQUIRED		
//		ssl-mode=REQUIRED
	}

	/** called by b at shutdown */
	@Override
	public void shutdown() throws Throwable {
		Db.log(getClass().getName() + ": shutdown");
		Db.instance().shutdown();
	}
}
