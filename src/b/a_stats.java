package b;

import b.a.stateless;

public @stateless class a_stats extends a implements bin{
	private static final long serialVersionUID=1L;

	@Override public String content_type(){
		return "text/plain;charset=utf-8";
	}

	@Override public void to(xwriter x) throws Throwable{
		thdwatch.update();
//		thdwatch.print_fieldnames_to(x.outputstream(),"\n");
//		thdwatch.print_fields_to(x.outputstream(),"\n");
//		x.nl();
//		b.stats_to(x.outputstream());
		thdwatch.print_fields3_to(x.outputstream());
	}

}
