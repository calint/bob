package bob;

import b.a;
import b.req;
import b.xwriter;

public class root extends a{
	static final long serialVersionUID=3;
	private int counter=0;
	public a txt;
	public a server_id;

	public root(){
		server_id.set(b.b.id);
	}

	public void to(final xwriter x) throws Throwable{
		x.style();
		x.css("body","padding:0 10em 0 4em");
		x.css(txt,"border-style:dotted;border-width:1px;border-color:green");
		x.style_();
		x.pl();
		x.p("server: ").span(server_id).nl();
		x.p("sesion id: ").p(req.get().session_id()).nl();
		x.inptxt(txt).p(" ");
		x.ax(this,"clk","click me");
	}

	public void x_clk(xwriter x,String s) throws Throwable{
		counter+=10;
		txt.set(txt.str()+" "+counter);
		x.xu(txt);
	}
}
