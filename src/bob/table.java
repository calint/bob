package bob;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import b.a;
import b.xwriter;

public class table extends a{
	static final long serialVersionUID=1;
	private final HashSet<String> selectedIds=new HashSet<String>();
	public a q; // query field

	public container ans; // actions
	public container cbs; // checkboxes

	public table(){
		final List<action> actions=getActionsList();
		for(action a:actions){
			ans.add(a);
		}

	}

	public void to(final xwriter x) throws Throwable{
		x.p("<div style='text-align:center;padding-bottom:0.5em'>");
		// add actions to container
		for(a e:ans.elements()){
			e.to(x);
			x.p(" ");
		}
		if(!ans.elements().isEmpty()){
			x.hr();
		}

		x.ax(this,"up","••").inpax(q,null,this,null);
		x.tag("is").p("$f('").p(q.id()).p("')").tage("is");
		x.div_();

		final List<?> ls=getList();

		x.table("f").nl();
		x.tr().th();
		renderHeaders(x);

		cbs.elements().clear();
		for(final Object o:ls){
			final String id=getIdFrom(o);
			x.tr().td();
			final checkbox cb=new checkbox(id,selectedIds.contains(id));
			cbs.add(cb);
			cb.to(x);
			renderRowCells(x,o);
			x.nl();
		}
		x.table_();
	}

	@Override protected void bubble_event(xwriter js,a from,Object o) throws Throwable{
		// event bubbled from child
		if(from instanceof checkbox){
			final String id=((checkbox)from).getId();
			if("checked".equals(o)){
				System.out.println("selected: "+id);
				selectedIds.add(id);
				return;
			}else if("unchecked".equals(o)){
				System.out.println("unselected: "+id);
				selectedIds.remove(id);
				return;
			}
		}
		if(from instanceof action){
			final action a=(action)from;
			a.process(this,selectedIds);
			xwriter x=js.xub(this,true,false);
			to(x);
			js.xube();
			return;
		}
		// event unknown by this element, bubble to parent
		super.bubble_event(js,from,o);
	}
	// callback for the query field
	public void x_(xwriter js,String s) throws Throwable{
		System.out.println("query: "+q.str());
		xwriter x=js.xub(this,true,false);
		to(x);
		js.xube();
	}
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	final static List<String> ls=new ArrayList<String>();
	static{
		ls.add("file1.txt");
		ls.add("file+2.txt");
		ls.add("another file.txt");
	}
	// --------------------------------------------------------------------------
	protected List<action> getActionsList(){
		final List<action> ls=new ArrayList<action>();
		ls.add(new action_create());
		ls.add(new action_delete());
		return ls;
	}
	protected List<?> getList(){
		final List<String> result=new ArrayList<String>();
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
	protected void renderRowCells(xwriter x,Object o){
		x.td().p(o.toString());
		x.td().p("2022-12-06 15:33");
		x.td().p("12 KB");
	}
}
