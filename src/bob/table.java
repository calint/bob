package bob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import b.a;
import b.xwriter;

public class table extends a{
	static final long serialVersionUID=1;
	private HashSet<String> selectedIds=new HashSet<String>();
	public a q;
	private ArrayList<checkbox> checkboxes=new ArrayList<checkbox>();
	public void to(final xwriter x) throws Throwable{
		x.p("<div style='text-align:center;padding-bottom:0.5em'>");
		x.ax(this,"up","••").inpax(q,null,this,null);
		x.tag("is").p("$f('").p(q.id()).p("')").tage("is");
		x.div_();

		final List<?> ls=getList();

		x.table("f").nl();
		x.tr().th();
		renderHeaders(x);
		checkboxes.clear();
		for(final Object o:ls){
			final String id=getIdFrom(o);
			x.tr().td();
			final checkbox cb=new checkbox(this,id,selectedIds.contains(id));
			checkboxes.add(cb);
			cb.to(x);
			renderCells(x,o);
			x.nl();
		}
		x.table_();
	}
	@Override protected a find_child(String name){
		for(final checkbox c:checkboxes){
			if(c.name().equals(name)){
				return c;
			}
		}
		return null;
	}
	@Override protected void bubble(xwriter x,a from,Object o) throws Throwable{
		// event bubbled from child
		if(from instanceof checkbox){
			final String name=((checkbox)from).name_unescaped();
			if("checked".equals(o)){
				selectedIds.add(name);
				return;
			}else if("unchecked".equals(o)){
				selectedIds.remove(name);
				return;
			}
		}
		// event unknown by this element, bubble to parent
		super.bubble(x,from,o);
	}
	public void x_(xwriter js,String s) throws Throwable{
		System.out.println("query: "+q.str());
		xwriter x=js.xub(this,true,false);
		to(x);
		js.xube();
	}
	public void x_sel(xwriter js,String s) throws Throwable{
		System.out.println("select: '"+s+"'");
		selectedIds.add(s);
		xwriter x=js.xub(this,true,false);
		to(x);
		js.xube();
	}
	public void x_unsel(xwriter js,String s) throws Throwable{
		System.out.println("unselect: '"+s+"'");
		selectedIds.remove(s);
		xwriter x=js.xub(this,true,false);
		to(x);
		js.xube();
	}
	// --------------------------------------------------------------------------
	final static List<String> ls=new ArrayList<String>();
	static{
		ls.add("file1.txt");
		ls.add("file+2.txt");
		ls.add("another file.txt");
	}
	// --------------------------------------------------------------------------
	protected List<?> getList(){
		final List<String>result=new ArrayList<String>();
		final String qstr=q.str().toLowerCase();
		for(Iterator<String> i=ls.iterator();i.hasNext();){
			final String title=i.next();
			if(!title.toLowerCase().startsWith(qstr)){
				continue;
			}
			result.add(title);
		}

		return result;
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
