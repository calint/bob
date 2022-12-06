package bob;

import java.util.ArrayList;
import java.util.List;
import b.a;
import b.xwriter;

public class table extends a{
	static final long serialVersionUID=3;
	public a q;
	public void to(final xwriter x) throws Throwable{
		x.table("f").nl();
		x.tr();
		x.th();
		x.ax(this,"up","••");
		x.th(3);
		x.span("margin-left:22px;float:right");
		x.inpax(q,null,this,null);
		x.tag("is").p("$f('").p(q.id()).p("')").tage("is");
		x.span_();

		final String icnfile="◻";
		final List<String> ls=new ArrayList<String>();
		ls.add("file1.txt");
		ls.add("file2.txt");
		ls.add("another file.txt");

		final String qstr=q.str();
		for(final String title:ls){
			if(!title.startsWith(qstr))
				continue;
			x.tr();
			x.td("icns").p(icnfile);
			x.td("name").p(title);
			x.td("date").p("2022-12-06 15:33");
			x.td("size").p("12 KB");
			x.nl();
		}
		x.tr().td().td().td();
		x.td("total size last").p("24 KB");
		x.nl();
		x.table_();
	}
	public void x_(xwriter xx,String s) throws Throwable{
		System.out.println("query: "+q.str());
		final xwriter xjs=new xwriter();
		xwriter x=xjs.xub(this,true,false);
		to(x);
		xjs.xube();
		xx.p(xjs.toString());
	}
}
