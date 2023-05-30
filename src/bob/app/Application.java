package bob.app;

import b.bapp;
import db.Db;
import db.test.Author;
import db.test.Book;
import db.test.Category;
import db.test.DataBinary;
import db.test.DataText;
import db.test.File;
import db.test.Game;
import db.test.Publisher;
import db.test.TestObj;
import db.test.User;

public final class Application implements bapp {
	private static Application inst;

	public static Application instance() {
		return inst;
	}

	public Application() {
		inst = this;
	}

	/** called by b at startup */
	public void init() throws Throwable {
//		Db.enable_log=false;
		Db.enable_log_sql = false;
		Db.register(User.class);
		Db.register(File.class);
		Db.register(DataBinary.class);
		Db.register(DataText.class);
		Db.register(Book.class);
		Db.register(Category.class);
		Db.register(Publisher.class);
		Db.register(Author.class);
		Db.register(Game.class);
		Db.register(TestObj.class);

		b.b.set_file_suffix_to_content_type("java", "text/plain");

		// bob.js uses this websocket
		b.b.set_path_to_class("/bob/websocket", WebSock.class);
		b.b.set_path_to_class("/stats", b.a_stats.class);
		b.b.set_path_to_class("/pbob", bob.app.ControllerPostback.class);

		// qa
		b.b.set_path_to_class("/b/test/t1", b.test.t1.class);
		b.b.set_path_to_class("/b/test/t2", b.test.t2.class);
		b.b.set_path_to_class("/b/test/t3", b.test.t3.class);
		b.b.set_path_to_class("/b/test/t4", b.test.t4.class);

		b.b.set_path_to_class("/db/test/t1", db.test.t1.class);
		b.b.set_path_to_class("/db/test/t2", db.test.t2.class);
		b.b.set_path_to_class("/db/test/t3", db.test.t3.class);

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
	public void shutdown() throws Throwable {
	}
}
