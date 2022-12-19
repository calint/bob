package db.test;

import java.io.PrintStream;

import b.a;
import b.a.stateless;
import b.xwriter;

public @stateless class t2 extends a {
	static final long serialVersionUID = 1;

	@Override
	public void to(final xwriter x) throws Throwable {
		runTest(x, 1, new import_books());
	}

	private void runTest(final xwriter x, final int nruns, final TestCase c) throws Throwable {
		c.out = new PrintStream(x.outputstream(), true);
		c.number_of_runs = nruns;
//		c.data_points_output = true;
		c.run();
	}
}
