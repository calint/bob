package bob;

import b.a;
import b.xwriter;

public class checkbox extends a{
	static final long serialVersionUID=1;
	final private static String off="◻";
	final private static String on="▣";
	private boolean checked;

	public checkbox(a parent,String name,boolean checked){
		super(parent,a.escape_html_name(name),checked?on:off);
		this.checked=checked;
	}
	public void to(xwriter x) throws Throwable{
		x.spano(this);
		if(checked){
			x.ax(this,"u",null,on);
		}else{
			x.ax(this,"s",null,off);
		}
		x.span_();
	}
	public void x_s(xwriter x,String param) throws Throwable{
		checked=true;
		set(on);
		bubble(x,this,"checked");
		x.xuo(this);
	}
	public void x_u(xwriter x,String param) throws Throwable{
		checked=false;
		set(off);
		bubble(x,this,"unchecked");
		x.xuo(this);
	}
}
