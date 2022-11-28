package a;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import b.a;
import b.b;
import b.path;
import b.req;
import b.xwriter;
import db.Db;
import db.test.Book;
public class diro extends a{
	private static final long serialVersionUID=1;
	public final static int BIT_ALLOW_QUERY=1<<0;
	public final static int BIT_ALLOW_FILE_LINK=1<<1;
	public final static int BIT_ALLOW_DIR_ENTER=1<<2;
	public final static int BIT_ALLOW_DIR_UP=1<<3;
	public final static int BIT_ALLOW_FILE_OPEN=1<<4;
	public final static int BIT_ALLOW_FILE_EDIT=1<<5;
	public final static int BIT_ALLOW_FILE_DELETE=1<<6;
	public final static int BIT_ALLOW_FILE_CREATE=1<<7;
	public final static int BIT_ALLOW_DIR_CREATE=1<<8;
	public final static int BIT_ALLOW_DIR_DELETE=1<<9;
	public final static int BIT_ALLOW_FILE_MODIFY=1<<10;
	public final static int BIT_ALLOW_SELECT=1<<11;
	public final static int BIT_ALLOW_MOVE=1<<12;
	public final static int BIT_ALLOW_RENAME=1<<13;
	public final static int BIT_ALLOW_COPY=1<<15;
	public final static int BIT_DISP_PATH=1<<16;
	public final static int BIT_ALLOW_LIST_WHEN_NO_QUERY=1<<17;
	public final static int BIT_ALL=-1;
	public a q;
	protected final SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.US);
	protected final NumberFormat nf=new DecimalFormat("###,###,###,###");
	protected int bits=BIT_DISP_PATH|BIT_ALLOW_QUERY|BIT_ALLOW_LIST_WHEN_NO_QUERY|BIT_ALLOW_FILE_LINK|BIT_ALLOW_DIR_ENTER|BIT_ALLOW_DIR_UP;
//	protected int bits=BIT_ALL;
	protected path root=b.path();
	protected path path=root;
	protected boolean sort=true;
	protected boolean sort_dirsfirst=true;
	public a bd;
	public final void root(final path root){this.root=root;if(!path.isin(root))path=root;}
	public final void bits(final int bits){this.bits=bits;}
	public final void bits_set(final int b){bits|=b;}
	public final void bits_clear(final int b){bits&=~b;}
	public final boolean hasbit(final int i){return (bits&i)!=0;}
	synchronized final public void to(final xwriter x) throws Throwable{
		x.tago("span").attr("id",id()).tagoe();
		final String[]files;
		final boolean isfile=path.isfile();
		final String query=q.toString();
		if(b.isempty(query)){
			if(hasbit(BIT_ALLOW_LIST_WHEN_NO_QUERY))
				files=path.list();
			else
				files=new String[0];
		}else{
			files=path.list(new FilenameFilter(){public boolean accept(File f,String s){
				return s.startsWith(query);
			}});
		}
		if(sort)
			sort(files);
		if(sort_dirsfirst)
			sort_dirsfirst(files);
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
		if(hasbit(BIT_ALLOW_QUERY))
			x.css(q,"float:right;background:yellow;border:1px dotted #555;text-align:right;width:10em;margin-left:1em");
		x.style_();
		x.style(selection,"display:table;padding-top:.5em;padding-bottom:.5em;margin-left:auto;margin-right:auto;background:#fefefe;text-align:center;box-shadow:0 0 .5em rgba(0,0,0,.5);");
		
		final int count=Db.currentTransaction().getCount(Book.class, null);
		x.p(count);
		
		x.table("f").nl();
		x.tr().th();
		if(hasbit(BIT_ALLOW_DIR_UP))
			if(!path.equals(root))
				x.ax(this,"up","••");
		final boolean acttd=hasbit(BIT_ALLOW_FILE_CREATE)||hasbit(BIT_ALLOW_DIR_CREATE);
		x.th(acttd?4:3);
		if(hasbit(BIT_DISP_PATH)){
			if(path.isin(root)){
				String pp=path.fullpath().substring(root.fullpath().length());
				x.span("float:left");
				x.p(pp);
				x.span_();
			}
		}
		final String icnfile="◻";
		final String icndir="⧉";
		final String icndel="x";
		final String icnsel="s";
		final String icnren="r";
		x.span("margin-left:22px;float:right");
		if(isfile){
			x.ax(this,"s",icnfile).spc();
			x.ax(this,"sx","▣");
			x.span_();
			x.nl();
		}else{
			if(hasbit(BIT_ALLOW_QUERY))
				x.inpax(q,null,this,null).focus(q);
			if(hasbit(BIT_ALLOW_FILE_CREATE))
				x.ax(this,"c",icnfile);
			if(hasbit(BIT_ALLOW_DIR_CREATE))
				x.ax(this,"d",icndir);
			x.span_();
			x.nl();
			long total_bytes=0;
			firstinlist=null;
			for(final String filenm:files){
				final path p;
				try{p=path.get(filenm);}catch(Throwable t){
					x.tr().td(99).p(filenm+"  "+t);
					continue;
				}
				if(firstinlist==null)firstinlist=p;
				final String fnm=p.name();
//				final String nameenc=b.urlencode(name);
				final boolean isdir=p.isdir();
				x.tr();
				x.td("icns");
				if(isdir)
					if((bits&BIT_ALLOW_DIR_ENTER)!=0)
						x.ax(this,"e "+fnm,icndir);
					else
						x.p(icndir);
				else
					if((bits&BIT_ALLOW_FILE_OPEN)!=0)
						x.ax(this,"e "+fnm,icnfile);
					else
						x.p(icnfile);
				
				//				x.p("<a href=\"javascript:ui.ax('").p(wid).p(" s ").p(nameEnc).p("')\">").p("↓").p("</a> ");
				//				x.p("<a href=\"javascript:ui.ax('").p(wid).p(" r ").p(nameEnc).p("')\">").p("ĸ").p("</a> ");
				x.td("name");
				if((bits&BIT_ALLOW_FILE_LINK)!=0&&p.isfile())
					x.a(p.uri(),fnm);
				else
					x.p(fnm);
				if(p.isfile()&&hasbit(BIT_ALLOW_FILE_DELETE))
					x.td("del").ax(this,"r "+fnm,icndel);				
				if(p.isdir()&&hasbit(BIT_ALLOW_DIR_DELETE))
					x.td("del").ax(this,"r "+fnm,icndel);
				if(hasbit(BIT_ALLOW_SELECT))
					x.ax(this,"se "+fnm,icnsel);
				if(hasbit(BIT_ALLOW_RENAME))
					x.ax(this,"ren "+fnm,icnren);
				
				x.td("date").p(ttoa(p.lastmod()));
				final long size=p.size();
				if(p.isfile())
					total_bytes+=size;
				x.td("size").p(isdir?"--":btoa(size));
				x.nl();
			}
			x.tr().td().td().td();
			if(acttd)x.td();
			x.td("total size last").p(nf.format(total_bytes));
			x.nl();
		}
		x.table_();
		if(isfile){
//			x.pre().nl().flush();
//			path.to(new osltgt(x.outputstream()));
			x.style().css(bd,"width:100%;height:1111em;border:1px dotted green").style_();
			x.inptxtarea(bd,"ed");
			x.focus(bd);
		}else{
			if(hasbit(BIT_ALLOW_SELECT)){
				selection.to(x);
			}
			x.focus(q);
		}
		x.nl();
		x.span_();
	}
	private path firstinlist;
	synchronized public void x_sel(final xwriter x,final String s)throws Throwable{
		if(firstinlist!=null)x_e(x,firstinlist.name());
	}
	synchronized public final void x_e(final xwriter x,final String p)throws Throwable{
		if(!hasbit(BIT_ALLOW_DIR_ENTER))throw new Error("notallowed");
		final String qr=queries.get(p.toString());
		q.set(qr);
		try{
			final path pp=path.get(p);
			path=pp;
			if(path.isfile())bd.from(path);
			q.clr();
		}catch(Throwable t){
			x.xalert(b.stacktrace(t));
		}
		x.xu(this);
 		x.xfocus(path.isfile()?bd:q);		
	}

	private void sort_dirsfirst(final String[]files){
		Arrays.sort(files,new Comparator<String>(){public int compare(final String a,final String b){try{
			final boolean da=path.get(a).isdir();
			final boolean db=path.get(b).isdir();
			if(da&&db)return 0;
			if(!da&&!db)return 0;
			if(da&&!db)return -1;
			if(!da&&db)return 1;
			return 0;
//		}catch(final Throwable t){throw new Error(t);}}});
		}catch(final Throwable ignored){return 0;}}});
	}
	private void sort(final String[]files){
		Arrays.sort(files,new Comparator<String>(){public int compare(final String a,final String b){
			return a.toString().toLowerCase(Locale.US).compareTo(b.toString().toLowerCase());
		}});
	}
	final protected String ttoa(final long ms){return df.format(ms);}
	final protected String btoa(final long n){return nf.format(n);}
//	private String query;
	private Map<String,String>queries=new HashMap<String,String>();
	synchronized public final void x_(final xwriter x,final String p)throws Throwable{
		if(!hasbit(BIT_ALLOW_QUERY))throw new Error("notallowed");
//		query=q.toString();
		if(q.toString().endsWith("/")){
//			q.clr();
			x_up(x,null);
			return;
		}
		queries.put(path.toString(),q.str());
		x.xuo(this);
//		to(x.xub(this));x.xube();
//		x.p("var e=$('").p(q.id()).p("');e.setSelectionRange(e.value.length,e.value.length)").nl();
		x.xfocus(q);
	}
	synchronized public final void x_up(final xwriter x,final String y)throws Throwable{
		if(!hasbit(BIT_ALLOW_DIR_UP))throw new Error("notallowed");
		final path p=path.parent();
		if(p==null)
			return;
		path=p.isin(root)?p:root;
		final String qr=queries.get(p.toString());
		q.set(qr);
		x.xu(this);
		x.xfocus(q);
	}
	synchronized public final void x_c(final xwriter x,final String s)throws Throwable{
		if(!hasbit(BIT_ALLOW_FILE_CREATE))throw new Error("notallowed");
		if(q.toString().length()==0){x.xalert("enter name");x.xfocus(q);return;}
		path=path.get(q.toString());
		if(!path.exists())
			path.append("");
		bd.from(path);
		x.xu(this);
		x.xfocus(bd);
	}
	synchronized public final void x_d(final xwriter x,final String s)throws Throwable{
		if(!hasbit(BIT_ALLOW_DIR_CREATE))throw new Error("notallowed");
		if(q.toString().length()==0){x.xalert("enter name");x.xfocus(q);return;}
		path.get(q.toString()).mkdirs();
		x.xu(this);
	}
	synchronized public final void x_r(final xwriter x,final String s)throws Throwable{
		final path p=path.get(s);
		if(path.isfile()&&!hasbit(BIT_ALLOW_FILE_DELETE))throw new Error("notallowed");
		if(path.isdir()&&!hasbit(BIT_ALLOW_DIR_DELETE))throw new Error("notallowed");//? onlydir
		p.rm();
		x.xu(this);
		x.xfocus(q);
	}
	synchronized public void x_s(final xwriter x,final String s)throws Throwable{
		if(!hasbit(BIT_ALLOW_FILE_MODIFY))throw new Error("notallowed");
		bd.to(path);
	}
	synchronized public void x_sx(final xwriter x,final String s)throws Throwable{x_s(x,s);x_up(x,"");}
	synchronized public void x_se(final xwriter x,final String s)throws Throwable{
		if(!hasbit(BIT_ALLOW_SELECT))throw new Error("notallowed");
		final path p=path.get(s);
		selection.add(p);
		if(x==null)return;
		x.xuo(selection);
	}
	synchronized public void x_ren(final xwriter x,final String s)throws Throwable{
		if(!hasbit(BIT_ALLOW_RENAME))throw new Error("notallowed");
		if(!path.get(s).rename(path.get(selection.rnm.toString()))){
			x.xalert("could not rename '"+s+"' to '"+selection.rnm+"'");
		}
		x.xuo(this);
	}
	
	
	
	
	
	
	
	
	
	
	final public static class selection extends a{
		static final long serialVersionUID=1;
		private diro dr;
		private LinkedList<path>ls=new LinkedList<path>();
		private boolean hidelist;
		public a rnm;
		public void to(final xwriter x) throws Throwable{
			x.el(this);
			x.style(rnm,"padding:.2em;border-bottom:1px dotted green;border-left:1px dotted greenl;width:12em;text-align:center");
			x.ax(this,"ts",hidelist?" show":" hide");
			if(hidelist){
				x.el_();
				return;
			}
			x.inptxt(rnm).br();
			x.tag("select").tag("option").p("selection").tage("select");
			x.table("f").nl();
			final int cols=5;
			x.nl().tr().th(cols);
			if(dr.hasbit(BIT_ALLOW_MOVE))x.ax(this,"m"," move");
			if(dr.hasbit(BIT_ALLOW_COPY))x.ax(this,"cp"," copy");

			final String icnfile="◻";
			final String icndir="⧉";
//			final String icndel="x";
			long total_bytes=0;
			final int root_path_len=req.get().session().path().fullpath().length();
			System.out.println(root_path_len);
			for(final path pth:ls){
//				final String name=pth.name();
				final boolean isdir=pth.isdir();
				x.nl().tr();

				x.td("icns");
				x.ax(this,"rm "+b.urlencode(pth.toString())," x ");
				x.p(isdir?icndir:icnfile);
				
				x.td("name");
				x.a(pth.uri(),pth.name());

				x.td("path");
				x.a(pth.uri(),pth.fullpath().substring(root_path_len));
				
				x.td("date").p(dr.ttoa(pth.lastmod()));
				
				final long size=pth.size();
				if(pth.isfile())total_bytes+=size;
				x.td("size").p(isdir?"--":dr.btoa(size));
			}
			x.tr();
			for(int i=0;i<cols-1;i++)x.td();
			x.td("total size last").p(dr.nf.format(total_bytes));
			x.nl().table_();
			x.el_();
		}
		public void x_ts(final xwriter x,final String s)throws Throwable{
			hidelist=!hidelist;
			x.xuo(this);
		}
		synchronized public void x_rm(final xwriter x,final String s)throws Throwable{
//			if(!dr.hasbit(BIT_ALLOW_FILE_DELETE))throw new Error("notallowed");
			for(final Iterator<path>i=ls.iterator();i.hasNext();){
				if(i.next().toString().equals(s)){
					i.remove();
					break;
				}
			}
			
			if(x==null)return;
			x.xuo(this);
		}
		synchronized public void x_m(final xwriter x,final String s)throws Throwable{
			if(!dr.hasbit(BIT_ALLOW_MOVE))throw new Error("notallowed");
			for(final path p:ls){
				if(!p.moveto(dr.path));//?
			}
			ls.clear();
			
			if(x==null)return;
			x.xuo(dr);
		}
		synchronized public void x_cp(final xwriter x,final String s)throws Throwable{
			if(!dr.hasbit(BIT_ALLOW_COPY))throw new Error("notallowed");
			for(final path p:ls){
				if(p.isdir())continue;//? recursivecopy
				p.copyto(dr.path);
			}
			
			if(x==null)return;
			x.xuo(dr);
		}
		public selection add(final path p){ls.add(p);return this;}
	}
	public selection selection;{selection.dr=this;}

}
