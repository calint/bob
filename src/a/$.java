package a;

import b.*;

public class $ extends a {
	static final long serialVersionUID = 1;
	public diro d;

	public $() {
//		d.root(req.get().session().path());
//		d.bits_set(diro.BIT_ALLOW_FILE_CREATE);
//		d.bits_set(diro.BIT_ALLOW_DIR_CREATE);
	}

	public void to(final xwriter x) throws Throwable {
		x.style().css("body", "padding:0 10em 0 4em").style_();
		d.to(x);
	}
}
