package c;

import b.a;
import b.xwriter;

public class app extends a{
	static final long serialVersionUID=3;
	private int counter=0;
	public a txt;
	
	public app(){
	}

	public void to(final xwriter x) throws Throwable{
		x.style();
		x.css("body","padding:0 10em 0 4em");
		x.css(txt,"border-style:dotted;border-width:1px;border-color:green");
		x.style_();
		x.pl();
		x.inptxt(txt).p(" ");
		x.ax(this,"clk","click me");
	}

	public void x_clk(xwriter x,String s) throws Throwable{
		counter+=10;
		txt.set(txt.str()+" "+counter);
		x.xu(txt);
	}
}
