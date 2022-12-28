package bob.app;

import bob.Controller;

/**
 * The main element in this bapp. The static field
 * websock_bob.controller_class_name is set to this class name.
 */
public class Ctrl extends Controller {
	private static final long serialVersionUID = 2L;

	public Ctrl() {
		m.addItem(TableFiles.class, "Files browser");
		bc.add(new TableFiles());
		m.addItem(TableBooks.class, "Books");
		m.addItem(TableCategories.class, "Categories");
		m.addItem(TablePublishers.class, "Publishers");
		m.addItem(TableDbClasses.class, "Classes");
	}
}
