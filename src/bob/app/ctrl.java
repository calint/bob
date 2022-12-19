package bob.app;

import bob.controller;

/**
 * The main element in this bapp. The static field
 * websock_bob.controller_class_name is set to this class name.
 */
public class ctrl extends controller {
	private static final long serialVersionUID = 2L;

	public ctrl() {
		m.addItem(table_files.class, "Files browser");
		bc.add(new table_files());
		m.addItem(table_dbclasses.class, "Classes");
		m.addItem(table_mock.class, "Mock");
		m.addItem(table_books.class, "Books");
	}
}
