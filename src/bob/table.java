package bob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import b.a;
import b.xwriter;

public class table extends a{
	static final long serialVersionUID=1;
	final static String icon_not_selected="◻";
	final static String icon_selected="▣";

	private HashSet<String> selectedIds=new HashSet<String>();
	public a q;
	public void to(final xwriter x) throws Throwable{
		x.p("<div style='text-align:center;padding-bottom:0.5em'>");
		x.ax(this,"up","••").inpax(q,null,this,null);
		x.tag("is").p("$f('").p(q.id()).p("')").tage("is");
		x.div_();

		final List<?> ls=getList();

		x.table("f").nl();
		x.tr().th();
		renderHeaders(x);
		for(final Object o:ls){
			final String id=getIdFrom(o);
			x.tr().td();
			final boolean selected=selectedIds.contains(id);
			if(selected){
				x.ax(this,"unsel",id,icon_selected);
			}else{
				x.ax(this,"sel",id,icon_not_selected);
			}
			renderCells(x,o);
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
	public void x_sel(xwriter js,String s) throws Throwable{
		System.out.println("selected: '"+s+"'");
		selectedIds.add(s);
		xwriter x=js.xub(this,true,false);
		to(x);
		js.xube();
	}
	public void x_unsel(xwriter js,String s) throws Throwable{
		System.out.println("unselected: '"+s+"'");
		selectedIds.remove(s);
		xwriter x=js.xub(this,true,false);
		to(x);
		js.xube();
	}
	
	//--------------------------------------------------------------------------
	protected List<?> getList(){
		final List<String> ls=new ArrayList<String>();
		ls.add("file1.txt");
		ls.add("file2.txt");
		ls.add("another file.txt");

		final String qstr=q.str().toLowerCase();
		for(Iterator<String> i=ls.iterator();i.hasNext();){
			final String title=i.next();
			if(!title.toLowerCase().startsWith(qstr)){
				i.remove();
			}
		}

		return ls;
	}
	protected String getIdFrom(Object o){
		return o.toString();
	}
	protected void renderHeaders(xwriter x){
		x.th().p("Name").th().p("Created").th().p("Size");
	}
	protected void renderCells(xwriter x,Object o){
		x.td().p(o.toString());
		x.td().p("2022-12-06 15:33");
		x.td().p("12 KB");
	}
}
