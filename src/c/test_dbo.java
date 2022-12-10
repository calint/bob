package c;

import b.a;
import b.xwriter;
import db.test.TestCase;
import db.test.import_books;
import db.test.import_games;
import db.test.test1;
import db.test.test2;
import db.test.test3;

public class test_dbo extends a{
	static final long serialVersionUID=1;

	public void to(final xwriter x) throws Throwable{
		x.style().css("body","padding:0 10em 0 4em").style_();
		final long t0=System.currentTimeMillis();
		runTest(x,new test1(true));
		runTest(x,new test2(true));
		runTest(x,new import_books(true));
		runTest(x,new import_games(true));
		runTest(x,new test3(true));
		final long dt=System.currentTimeMillis()-t0;
		x.p("done: ").p(dt/1000).pl(" s");
	}

	private void runTest(xwriter x,TestCase c) throws Throwable{
		x.p(c.getTestName()).pl(" start").flush();
		c.run();
		x.p(c.getTestName()).pl(" done").flush();
	}
}
