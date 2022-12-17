package bob.elem;

import bob.controller;

/**
 * The main element in this bapp. The static field
 * websock_bob.controller_class_name is set to this class name.
 */
public class ctrl extends controller {
	private static final long serialVersionUID = 1L;

	public ctrl() {
//		bc.add(new table_mock());
		bc.add(new table_files());
	}
}
