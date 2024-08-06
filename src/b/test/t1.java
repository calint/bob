package b.test;

import b.a;
import b.a.stateless;
import b.xwriter;

/** hello world */
public @stateless class t1 extends a {
    private static final long serialVersionUID = 1;

    @Override
    public void to(final xwriter x) throws Throwable {
        x.title(getClass().getName());
        x.pl("hello world");
    }
}
