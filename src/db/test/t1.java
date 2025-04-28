package db.test;

import java.io.PrintStream;

import b.a;
import b.a.stateless;
import b.bin;
import b.xwriter;

public @stateless class t1 extends a implements bin {

    static final long serialVersionUID = 1;

    public String content_type() {
        return "text/plain";
    }

    @Override
    public void to(final xwriter x) throws Throwable {
        runTest(x, 1000, new test1());
    }

    private void runTest(final xwriter x, final int nruns, final TestCase c) throws Throwable {
        c.out = new PrintStream(x.outputstream(), true);
        c.number_of_runs = nruns;
        c.data_points_output = true;
        c.run();
    }

}
