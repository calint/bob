package c;

import java.io.PrintStream;

import b.a;
import b.a.stateless;
import b.xwriter;
import db.test.TestCase;
import db.test.test1;

public @stateless class test_dbo extends a{
	static final long serialVersionUID=1;

	public void to(final xwriter x) throws Throwable{
		x.style().css("body","padding:0 10em 0 4em").style_();
		final long t0=System.currentTimeMillis();
		runTest(x,100,new test1());
//		runTest(x,new test2());
//		runTest(x,new import_books(false));
//		runTest(x,new import_games(false));
//		runTest(x,new test3());
		final long dt=System.currentTimeMillis()-t0;
		x.p("done: ").p(dt).pl(" ms");
	}

	private void runTest(xwriter x,int nruns,TestCase c) throws Throwable{
		c.out=new PrintStream(x.outputstream(),true);
		c.number_of_runs=nruns;
//		x.p(c.getTestName()).pl(" start").flush();
		c.run();
//		x.p(c.getTestName()).pl(" done").flush();
	}
}
