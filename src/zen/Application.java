package zen;

import b.bapp;

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
		b.b.set_path_to_class("/zen/websocket", zen.WebSock.class);
	}

	/** called by b at shutdown */
	public void shutdown() throws Throwable {
	}
}
