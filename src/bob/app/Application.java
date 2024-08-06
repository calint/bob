// reviewed: 2024-08-05
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
    public static final Application instance = new Application();

    /** called by b at startup */
    public void init() throws Throwable {
        Db.enable_log = false;
        Db.enable_log_sql = true;
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

        b.b.set_path_to_class("/bob/websocket", WebSock.class);
        b.b.set_path_to_class("/stats", b.a_stats.class);
        b.b.set_path_to_class("/pbob", bob.app.ControllerPostback.class);

        // qa
        b.b.set_path_to_class("/b/test/t1", b.test.t1.class);
        b.b.set_path_to_class("/b/test/t2", b.test.t2.class);
        b.b.set_path_to_class("/b/test/t3", b.test.t3.class);
        b.b.set_path_to_class("/b/test/t4", b.test.t4.class);

        b.b.set_path_to_class("/db/test/t1", db.test.t4.class);
        b.b.set_path_to_class("/db/test/t2", db.test.t2.class);
        b.b.set_path_to_class("/db/test/t3", db.test.t3.class);
        b.b.set_path_to_class("/db/test/t4", db.test.t4.class);
    }

    /** called by b at shutdown */
    public void shutdown() throws Throwable {
    }
}
