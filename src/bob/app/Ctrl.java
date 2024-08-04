package bob.app;

import bob.Controller;

/** main element in this bapp */
public final class Ctrl extends Controller {
	private static final long serialVersionUID = 3L;

	public Ctrl() {
		m.addItem(TableFsFiles.class, "Files browser");
		bc.add(new TableFsFiles());
		m.addItem(TableBooks.class, "Books");
		m.addItem(TableCategories.class, "Categories");
		m.addItem(TableAuthors.class, "Authors");
		m.addItem(TablePublishers.class, "Publishers");
		m.addItem(TableUsers.class, "Users");
		m.addItem(TableFiles.class, "Files");
		m.addItem(TableDbClasses.class, "Classes");
	}
}
