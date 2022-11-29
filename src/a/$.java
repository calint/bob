package a;

import b.*;
import db.Db;
import db.test.Book;

public class $ extends a {
	static final long serialVersionUID = 1;
	private int counter = 0;
	public a txt;
//	public diro d;

	public $() {
//		d.root(req.get().session().path());
//		d.bits_set(diro.BIT_ALLOW_FILE_CREATE);
//		d.bits_set(diro.BIT_ALLOW_DIR_CREATE);
	}

	public void to(final xwriter x) throws Throwable {
		x.style().css("body", "padding:0 10em 0 4em").style_();
		final int count = Db.currentTransaction().getCount(Book.class, null);
		x.p(count).p(" server id: ").p(b.id).p("  counter:").p(++counter).nl();
		x.inptxt(txt);
		x.ax(this, "clk", "click me");
//		d.to(x);
	}

	public void x_clk(xwriter x,String s) {
		counter+=10;
		x.xreload();
	}
}
