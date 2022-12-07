package bob;

import java.util.ArrayList;
import java.util.List;
import b.a;
import b.xwriter;

public class table extends a{
	static final long serialVersionUID=1;
	public a q;
	public void to(final xwriter x) throws Throwable{
		x.p("<div style='text-align:center;padding-bottom:0.5em'>");
		x.ax(this,"up","••").inpax(q,null,this,null);
		x.tag("is").p("$f('").p(q.id()).p("')").tage("is");
		x.div_();

		final List<String> ls=new ArrayList<String>();
		ls.add("file1.txt");
		ls.add("file2.txt");
		ls.add("another file.txt");
		
		final String icon_not_selected="◻";
		final String icon_selected="▣";
		x.table("f").nl();
		x.tr().th().th().p("Name").th().p("Created").th().p("Size");
		final String qstr=q.str();
		for(final Object o:ls){
			final String title=(String)o;
			if(!title.toLowerCase().startsWith(qstr.toLowerCase()))
				continue;
			x.tr();
			x.td().p(icon_not_selected);
			x.td().p(title);
			x.td().p("2022-12-06 15:33");
			x.td().p("12 KB");
			x.nl();
		}
		x.table_();
	}
	public void x_(xwriter js,String s) throws Throwable{
		System.out.println("query: "+q.str());
		xwriter x=js.xub(this,true,false);
		to(x);
		js.xube();
	}
}
