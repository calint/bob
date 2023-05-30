package zen;

import b.bapp;
import db.Db;

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
		Db.log(getClass().getName() + ": init");
		// Db.enable_log=false;
		Db.enable_log_sql = false;

		b.b.set_file_suffix_to_content_type("java", "text/plain");

		// bob.js uses this websocket
		b.b.set_path_to_class("/bob/websocket", zen.WebSock.class);
	}

	/** called by b at shutdown */
	public void shutdown() throws Throwable {
		Db.log(getClass().getName() + ": shutdown");
		Db.shutdown();
	}
}
