//
// reviewed: 2025-04-29
//
package zen;

import b.bapp;

public final class Application implements bapp {

    /** called by `b` at startup */
    public void init() throws Throwable {
        b.b.set_path_to_class("/zen/websocket", zen.WebSock.class);
    }

    /** called by `b` at shutdown */
    public void shutdown() throws Throwable {
    }

}
