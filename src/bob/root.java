package bob;

import java.util.ArrayList;
import java.util.List;

import b.a;
import b.path;
import b.req;
import b.xwriter;

public class root extends a{
	static final long serialVersionUID=3;
	private int counter=0;
	public a txt;
	public a q;

//	public a server_id;

	public root(){
//		server_id.set(b.b.id);
	}

	public void to(final xwriter x) throws Throwable{
		x.style();
		x.css(txt,"border-style:dotted;border-width:1px;border-color:green");
		x.style_();
//		x.p("server: ").span(server_id).nl();
		x.p("server: ").p(b.b.id).p("  sesion id: ").p(req.get().session_id()).nl();
		x.inptxt(txt).p(" ");
		x.ax(this,"clk","click me");

		x.style();
		x.css("table.f","margin-left:auto;margin-right:auto");
		x.css("table.f tr:first-child","border:0;border-bottom:1px solid green;border-top:1px solid #070");
		x.css("table.f tr:last-child td","border:0;border-top:1px solid #040");
		x.css("table.f th:first-child","border-right:1px dotted #ccc");
		x.css("table.f td","padding:.5em;vertical-align:middle;border-left:1px dotted #ccc;border-bottom:1px dotted #ccc");
		x.css("table.f td:first-child","border-left:0");
		x.css("table.f td.icns","text-align:center");
		x.css("table.f td.size","text-align:right");
		x.css("table.f td.total","font-weight:bold");
		x.css("table.f td.name","min-width:100px");
		x.css("table.f th","padding:.5em;text-align:left;background:#f0f0f0;color:black");
		x.css(q,"float:right;background:yellow;border:1px dotted #555;text-align:right;width:10em;margin-left:1em");
		x.style_();

		x.table("f").nl();
		x.tr();
		x.th();
		x.ax(this,"up","••");
		x.th(3);
		x.span("margin-left:22px;float:right");
		x.inpax(q,null,this,null);
		x.p("<is>").p("$f('").p(q.id()).p("')").p("</is>");
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

	public void x_clk(xwriter x,String s) throws Throwable{
		counter+=10;
//		final byte[] ba=new byte[20*1024*1024];
//		byte ch=(byte)'a';
//		for(int i=0;i<ba.length;i++){
//			ba[i]=ch;
//			ch++;
//			if(ch>(byte)'z')
//				ch='a';
//		}
//		final String msg=new String(ba,0,ba.length);
//		txt.set(msg);
//		Thread.sleep(10*1000);
		txt.set(txt.str()+" "+counter);
		x.xu(txt);
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
