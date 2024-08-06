package zen;

import b.bapp;

public final class Application implements bapp {

    public final static Application instance = new Application();

    /** called by b at startup */
    public void init() throws Throwable {
        b.b.set_path_to_class("/zen/websocket", zen.WebSock.class);
    }

    /** called by b at shutdown */
    public void shutdown() throws Throwable {
    }

}
