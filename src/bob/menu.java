package bob;

import java.util.ArrayList;
import b.a;
import b.xwriter;

public class menu extends a{
	private static final long serialVersionUID=1L;
	private final static class item extends a{
		private static final long serialVersionUID=1L;
		Class<? extends a> cls;
		String title;
		public item(Class<? extends a> cls,String title){
			super();
			this.cls=cls;
			this.title=title;
		}

	}

	private final ArrayList<item> items=new ArrayList<item>();

	@Override public void to(xwriter x) throws Throwable{
		final String id=id();
		x.tago("select").default_attrs_for_element(this).attr("onchange","$x('"+id+" s '+this.selectedIndex)");
		x.tagoe();
		int i=0;
		for(item im:items){
			x.tago("option").attr("value",i).tagoe().p(im.title);
			i++;
		}
		x.tage("select");
	}

	public void addItem(Class<? extends a> cls,String title){
		items.add(new item(cls,title));
	}

	public void x_s(xwriter x,String param) throws Throwable{
		super.bubble_event(x,this,items.get(Integer.parseInt(param)).cls);
	}
}
