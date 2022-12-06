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
		x.tr().th().ax(this,"up","••").th(3).span("margin-left:22px;float:right").inpax(q,null,this,null).span_();;

//		x.tag("is").p("alert('hello \\'hello\\'')").tage("is");
		x.tag("is").p("$f('").p(q.id()).p("')").tage("is");
		final String icon_not_selected="◻";
		final String icon_selected="▣";
		final List<String> ls=new ArrayList<String>();
		ls.add("file1.txt");
		ls.add("file2.txt");
		ls.add("another file.txt");
		
		x.tr().th().th().p("Name").th().p("Created").th().p("Size");
		final String qstr=q.str();
		for(final String title:ls){
			if(!title.startsWith(qstr))
				continue;
			x.tr();
			x.td("icns").p(icon_not_selected);
			x.td("name").p(title);
			x.td("date").p("2022-12-06 15:33");
			x.td("size").p("12 KB");
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
