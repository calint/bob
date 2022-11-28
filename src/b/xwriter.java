package b;
import static b.b.isempty;
import static b.b.tobytes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
public final class xwriter{
	public xwriter(final OutputStream os){this.os=os;}
	public xwriter(){os=new ByteArrayOutputStream();}
	public OutputStream outputstream(){return os;}
	public xwriter p(final String s){if(s==null)return this;try{os.write(tobytes(s));}catch(final IOException e){throw new Error(e);}return this;}
	public xwriter nl(){return p('\n');}
	public xwriter nl(final int number_of_newlines){for(int i=0;i<number_of_newlines;i++)p('\n');return this;}
	public xwriter p(final byte n){return p(Byte.toString(n));}
	public xwriter p(final char n){return p(Character.toString(n));}
	public xwriter p(final int n){return p(Integer.toString(n));}
	public xwriter p(final float n){return p(Float.toString(n));}
	public xwriter p(final long n){return p(Long.toString(n));}
	public xwriter p(final double n){return p(Double.toString(n));}
	public xwriter pl(final String s){return p(s).nl();}
	public xwriter pl(){return nl();}
	public xwriter tag(final String name){return p("<").p(name).p(">");}
	public xwriter tag(final String name,final String id){return p("<").p(name).p(" id=").p(id).p(">");}
	public xwriter tago(final String name){return p("<").p(name);}
	public xwriter attrdef(final a e){final String id=e.id();return attr("id",id);}
	public xwriter attr(final String name,final int value){return p(" ").p(name).p("=").p(value);}
	public xwriter attr(final String name,final String value){return p(" ").p(name).p("=\"").p(encquot(value)).p("\"");}
	public xwriter attr(final String name){return p(" ").p(name);}
	public xwriter tagoe(){return p(">");}
	public xwriter tage(final String name){return p("</").p(name).p(">");}
	public xwriter a(final String href){return tago("a").attr("href",href).tagoe();}
	public xwriter a_(){return tage("a");}
	public xwriter a(final String href,final String txt){return a(href).p(txt).a_();}
	public xwriter ax(final a e,final String func){return ax(e,func,func);}
	public xwriter ax(final a e,final String func,final String html){
		final String wid=e.id();
		p("<a href=\"javascript:").axjs(wid,func,"").p("\">").p(html).p("</a>");
		return this;
	}
	public xwriter ax(final a e,final String func,final String param,final String html){
		final String wid=e.id();
		p("<a href=\"javascript:").axjs(wid,func,param).p("\">").p(html).p("</a>");
		return this;
	}
	public xwriter ax(final a e,final String func,final String param,final String html,final String accesskey){
		final String wid=e.id();
		p("<a");
		if(accesskey!=null)spc().p("accesskey=").p(accesskey).attr("title",accesskey);
		p(" href=\"javascript:").axjs(wid,func,param).p("\">").p(html).p("</a>");
		return this;
	}
	public xwriter axjs(final String eid,final String func,final String param){
		p("$x('").p(eid);
		if(!isempty(func))p(" ").p(func);
		if(!isempty(param))p(" ").p(param);
		return p("')");
	}
	public xwriter ajx(final a e,final String args){return tago("a").attr("href","javascript:$x('"+e.id()+" "+args+"')").attr("id",e.id()).tagoe();}
	public xwriter ajx(final a e){return tago("a").attr("href","javascript:$x('"+e.id()+"')").tagoe();}
	public xwriter ajx_(){return tage("a");}
	public xwriter br(){return tag("br");}
//	public xwriter divo(final String cls){return tago("div").attr("class",cls).tagoe();}
	public xwriter divo(){return tag("div");}
	public xwriter divo(final a e){return tago("div").attr("id",e.id()).tagoe();}
	public xwriter divo(final a e,final String cls){return divo(e,cls,"");}
	public xwriter divo(final String cls,final String style){return divo(null,cls,style);}
	public xwriter divo(final String cls){return divo(null,cls,null);}
	public xwriter divo(final a e,final String cls,final String style){
		tago("div");
		if(e!=null)attr("id",e.id());
		if(!isempty(cls))attr("class",cls);
		if(!isempty(style))attr("style",style);
//		if(!isempty(intaginline))spc().p(intaginline);
		return tagoe();
	}
	public xwriter divh(final a e){return divh(e,null,null);}
	public xwriter divh(final a e,final String cls){return divh(e,cls,null);}
	public xwriter divh(final a e,final String cls,final String style){
		tago("div");
		if(e!=null)attr("id",e.id());
		if(!isempty(cls))attr("class",cls);
		if(!isempty(style))attr("style",style);
//		if(!isempty(intaginline))spc().p(intaginline);
		tagoe();
		try{e.to(this);}catch(Throwable t){throw new Error(t);}//? printerror
		return div_();
	}
//	public xwriter ph_div(final a e){return tago("div").attr("id",e.id()).tagoe().tage("div");}
	public xwriter div_(){return tage("div");}
	public xwriter focus(final a e){return script().p("$f('").p(e.id()).p("')").script_();}
	public xwriter inpint(final a e){return tago("input").attr("value",e.toString()).attrdef(e).attr("type","text").attr("class","nbr").attr("size",5).attr("onchange","$b(this)").tagoe();}
	public xwriter inpflt(final a e){return tago("input").attr("value",e.toString()).attrdef(e).attr("type","text").attr("class","nbr").attr("size",5).attr("onchange","$b(this)").tagoe();}
	public xwriter inplng(final a e){return inpint(e);}
	public xwriter pre(){return tag("pre");}
	public xwriter pre(final String cls){return tago("pre").attr("class",cls).tagoe();}
	public xwriter pre_(){return tage("pre");}
	public xwriter script(){return tag("script");}
	public xwriter script_(){return tage("script");}
	public xwriter spanx(final a e){return spanx(e,null);}
	public xwriter spanx(final a e,final String style){
		tago("span").attr("id",e.id());
		if(style!=null)attr("style",style);
		return tagoe().tage("span");
	}
	public xwriter span(final a e){return span(e,null);}
	public xwriter span(final a e,final String style){
		tago("span").attr("id",e.id());
		if(style!=null)attr("style",style);
		tagoe();
		try{e.to(new osltgt(os));}catch(Throwable t){throw new Error(t);}
		return span_();
	}
	public xwriter spanh(final a e){return spanh(e,null,null);}
	public xwriter spanh(final a e,final String cls){return spanh(e,cls,null);}
	public xwriter spanh(final a e,final String cls,final String style){
		tago("span").attr("id",e.id());
		if(cls!=null)attr("class",cls);
		if(style!=null)attr("style",style);
		tagoe();
		try{e.to(os);}catch(Throwable t){throw new Error(t);}
		return span_();
	}
	public xwriter span_(){return tage("span");}
	public xwriter table(){return tag("table");}
	public xwriter table(final String cls){return tago("table").attr("class",cls).tagoe();}
	public xwriter table(final String cls,final String style){
		tago("table");
		if(!isempty(cls))attr("class",cls);
		if(!isempty(style))attr("style",style);
		tagoe();
		return this;
	}
	public xwriter table_(){return tage("table");}
	public xwriter style(){return p("<style scoped>");}
	public xwriter style_(){return tage("style");}
	public xwriter td(){return tag("td");}
	public xwriter td(final String cls){return td(cls,"");}
	public xwriter td(final String cls,final String style){
		tago("td");
		if(cls!=null&&cls.length()!=0)
			attr("class",cls);
		if(style!=null&&style.length()!=0)
			attr("style",style);
		return tagoe();
	}
	public xwriter td_(){return tage("td");}
	public xwriter th(){return tag("th");}
	public xwriter th(final int colspan){return tago("th").attr("colspan",colspan).tagoe();}
	public xwriter th(final String cls){return tago("th").attr("class",cls).tagoe();}
	public xwriter th_(){return tage("th");}
	public xwriter tr(){return tag("tr");}
	public xwriter tr(final String cls){return tago("tr").attr("class",cls).tagoe();}
	public xwriter tr_(){return tage("tr");}
	public xwriter ul(){return tag("ul");}
	public xwriter ul_(){return tage("ul");}
	public xwriter li(){return tag("li");}
	public xwriter li(final String cls){if(cls==null)return li();return tago("li").attr("class",cls).tagoe();}
	public xwriter code(){return tag("code");}
	public xwriter code_(){return tage("code");}
//	public xwriter rend(final a e)throws Throwable{if(e==null)return this;e.to(this);return this;}
	public xwriter inptxt(final a e){return inp(e,"text",null,null,null,null,null,null,null);}
	public xwriter inptxt(final a e,final a axonreturn){return inp(e,"text",null,null,axonreturn,null,null,null,null);}
	public xwriter inptxt(final a e,final a axonreturn,final String axp){return inp(e,"text",null,null,axonreturn,axp,null,null,null);}
	public xwriter inptxt(final a e,final a axonreturn,final String axp,final String stylecls){return inp(e,"text",null,stylecls,axonreturn,axp,null,null,null);}
	public xwriter inptxt(final a e,final a axonreturn,final String axp,final String txt,final String stylecls){return inp(e,"text",null,stylecls,axonreturn,axp,txt,null,null);}
	public xwriter inpcolr(final a e){return inp(e,"color",null,null,null,null,null,null,null);}
	public xwriter inp(final a e,final String type,final String style,final String stylecls,final a on_enter_ajax_elem,final String on_enter_ajax_param,final String txt,final a on_change_ajax_elem,final String on_change_ajax_param){
		final String value=txt==null?e.toString():txt;
		tago("input").attr("value",value).attrdef(e);
		if(!isempty(type))attr("type",type);
		if(!isempty(stylecls))attr("class",stylecls);
		if(!isempty(style))attr("style",style);
		if(on_enter_ajax_elem!=null){
			final String ax=on_enter_ajax_elem.id()+(on_enter_ajax_param!=null?(" "+on_enter_ajax_param):"");
			attr("onkeypress","return $r(event,this,'"+ax+"')");
		}
		final StringBuilder sb=new StringBuilder();
		if("checkbox".equals(type)){
			if(value.equals(Boolean.TRUE.toString()))attr("checked","checked");
			sb.append("this.value=this.checked?'1':'0';$b(this)");
			if(on_change_ajax_elem!=null){
				final String ax=on_enter_ajax_elem.id()+(on_enter_ajax_param!=null?(" "+on_change_ajax_param):"");
				sb.append(";$x('"+ax+"')");
			}		
		}else{
			sb.append("$b(this)");
			if(on_change_ajax_elem!=null){
				final String ax=on_enter_ajax_elem.id()+(on_enter_ajax_param!=null?(" "+on_change_ajax_param):"");
				sb.append(";$x('"+ax+"')");
			}
		}
		attr("onchange",sb.toString());
		return tagoe();//? <input hidden value="false">
	}
	public xwriter inptxtarea(final a e){return inptxtarea(e,null);}
	public xwriter inptxtarea(final a e,final String cls){
		tago("textarea").attrdef(e)
			.attr("onchange","$b(this)")
//			.attr("onkeydown","$b(this);if(event.keyCode==9){console.log(this.id+' '+this.selectionStart);this.value=this.value.substring(0,this.selectionStart)+'   '+this.value.substring(this.selectionStart,this.value.length);console.log(this.selectionStart);this.selectionStart=this.selectionEnd=this.selectionStart-2;return false;}return true;")
			.attr("onkeydown","$b(this)")
			;
			
		if(cls!=null)attr("class",cls);
		attr("wrap","off").attr("spellcheck","false").tagoe();
		try{e.to(new osltgt(outputstream()));}catch(final Throwable t){b.log(t);p(b.stacktrace(t));}
		return tage("textarea");
	}
	public xwriter flush(){try{os.flush();return this;}catch(final IOException e){throw new Error(e);}}
	public String toString(){return os.toString();}
	public xwriter hr(){return tag("hr");}
	public xwriter spc(){return p(' ');}
	public xwriter tab(){return p('\t');}
	public xwriter enter(){return p('\r');}
	public xwriter bell(){return p('\07');}
	public xwriter p(final CharSequence cs){return p(cs.toString());}
	public xwriter title(final String s){return tag("title").p(s).tage("title");}
	public xwriter css(final String selector,final String stl){return p(selector).p("{").p(stl).p("}");}
	public xwriter css(final a e,final String style){return p("#").p(e.id()).p("{").p(style).p("}");}
	public xwriter inpax(final a e,final String stylecls,final a ax,final String axp){
		tago("input").attr("value",e.toString()).attrdef(e).attr("type","text");
		if(!isempty(stylecls))attr("class",stylecls);
		attr("onfocus","this.setSelectionRange(this.value.length,this.value.length)");
		final StringBuilder sb=new StringBuilder();
		sb.append((ax==null?e:ax).id());
		if(!isempty(axp))sb.append(" ").append(axp);
		final String sbs=sb.toString();
		attr("oninput","$b(this);$x('"+sbs+"');return true;");
		attr("onkeypress","return $r(event,this,'"+ax.id()+" sel')");
		return tagoe();
	}
	public xwriter inpax(final a e,final String stylecls,final a ax,final String onchangeaxp,final String onselectaxp){
		tago("input").attr("value",e.toString()).attrdef(e).attr("type","text");
		if(!isempty(stylecls))attr("class",stylecls);
		final String eid=ax.id();
		attr("onfocus","this.setSelectionRange(this.value.length,this.value.length)");
		attr("oninput","$b(this);$x('"+eid+" "+onchangeaxp+"');return true;");
		if(onselectaxp!=null)attr("onkeypress","if(!event)event=window.event;if(event.keyCode!=13)return true;$x('"+eid+" "+onselectaxp+"');return false;");
		return tagoe();
	}
	public xwriter inputax(final a e){return inpax(e,null,e.pt(),null);}
	public xwriter output_holder(final a e){return tago("output").attr("id",e.id()).tagoe().tage("output");}//? value
	public xwriter spc(final int n){for(int i=0;i<n;i++)spc();return this;}
	public xwriter ul(final String cls){return tago("ul").attr("class",cls).tagoe();}
	public xwriter cssfont(final String name,final String url){return p("@font-face{font-family:").p(name).p(";src:url(").p(url).p(");}");}
	@SuppressWarnings("resource") // osjsstr does not need to do any cleanup
	public xwriter jsstr(final String s){try{new osjsstr(os).write(tobytes(s));return this;}catch(final IOException e){throw new Error(e);}}
	public xwriter xu(final String id,final String s){return p("$s('").p(id).p("','").jsstr(s).pl("');");}
	public xwriter xu(final a e,final String s){return xu(e.id(),s);}
	public xwriter xub(final a e,final boolean inner,final boolean escltgt){
		p("$").p(inner?"s":"o").p("('").p(e.id()).p("','");
		return new xwriter(new osjsstr(escltgt?new osltgt(os):os));
	}
	public xwriter xube(){return pl("');");}
	public xwriter xu(final a e)throws Throwable{e.to(xub(e,true,false));return xube();}
	public xwriter xu(final a...es)throws Throwable{
		for(a e:es){
			e.to(xub(e,true,false));xube();
		}
		return this;
	}
	public xwriter xu(final a e,final boolean escltgt)throws Throwable{e.to(xub(e,true,escltgt));return xube();}
	public xwriter xuo(final a e)throws Throwable{e.to(xub(e,false,false));return xube();}
	public xwriter xinterval(final a e,final String ax,final int ms){return p("setInterval(\"$x('").p(e.id()).p(" ").p(ax).p("')\",").p(ms).pl(");");}
	private xwriter xhide(final a e,final boolean hide){
		//? bug style block  display:inherit
		return p("$('").p(e.id()).p("').style.display='").p(hide?"none":"inline").pl("';");
	}
	public xwriter xhide(final a e){return xhide(e,true);}
	public xwriter xshow(final a e){return xhide(e,false);}
	public xwriter xalert(final String s){return p("ui.alert('").jsstr(s).pl("');");}
	public xwriter xreload(){return pl("location.reload(true);");}
	public xwriter xfocus(final a e){return p("$f('").p(e.id()).pl("');");}
	public xwriter xfocus(final String id){return p("$f('").p(id).pl("');");}
	public xwriter xtitle(final String s){return p("$t('").jsstr(b.isempty(s,"")).pl("');");}
	public xwriter xp(final a e,final String s){return p("$p('").p(e.id()).p("','").jsstr(s).pl("');");}
	public xwriter el(){return p("<div style=display:inline>");}
//	public xwriter el(){return p("<span>");}
	public xwriter el(final String style){return p("<div style=\"display:inline;").p(style).p("\">");}
//	public xwriter el(final String style){return p("<span style=\"").p(style).p("\">");}
	public xwriter el(final a e){return p("<div style=display:inline id=").p(e.id()).p(">");}
//	public xwriter el(final a e){return p("<span id=").p(e.id()).p(">");}
	public xwriter el(final a e,final String style){return p("<div id=").p(e.id()).p(" style=\"display:inline;").p(style).p("\">");}
//	public xwriter el(final a e,final String style){return p("<span id=").p(e.id()).p(" style=\"").p(style).p("\">");}
	public xwriter el_(){return p("</div>");}
//	public xwriter el_(){return p("</span>");}
	public xwriter r(final a e)throws Throwable{if(e==null)return this;e.to(this);return this;}
	public xwriter rel(final a e)throws Throwable{
		if(e==null)return this;
		el(e);
		e.to(this);
		return el_();
	}
	public xwriter rdiv(final a e)throws Throwable{
		if(e==null)return this;
		divo(e);
		e.to(this);
		return div_();
	}
	public xwriter td(final int colspan){return p("<td colspan=").p(colspan).p(">");}
	public xwriter style(final a e,final String style){return style().css(e,style).style_();}
	public xwriter style(final a e,final String selector,final String style){return style().css(e,selector,style).style_();}
	public xwriter style(final String selector,final String style){return style().css(selector,style).style_();}
	public xwriter ax(final a e){return ax(e,"","::");}
	public xwriter dived(final a e,final a axe,final String axp,final String style)throws Throwable{
		p("<div id=").p(e.id()).p(" contenteditable=true spellcheck=false ");
		if(style!=null&&style.length()>0)
			p("style=\"").p(style).p("\" ");
		p("onkeypress=\"");
		if(axe!=null){
			p("if(event.charCode==13){$x('");
			p(axe.id());
			if(axp!=null&&axp.length()>0){
				p(" ");
				p(axp);
			}
			p("');return false;}");
		}
		p("$b(this)\">");
		e.to(os);
		return p("</div>");
	}
	public xwriter dived(final a e,final a axe,final String axp)throws Throwable{return dived(e,axe,axp,null);}
	public xwriter dived(final a e)throws Throwable{return dived(e,null,null,null);}
	public xwriter css(final a e,final String selector,final String style){return css("#"+e.id()+" "+selector,style);}
	public xwriter ol(){return tag("ol");}
	public xwriter ol_(){return tage("ol");}
	public xwriter xlocation(final String uri){return p("location='").p(uri).pl("';");}
	public xwriter span(final String style){
		return span("",style);
	}
	public xwriter span(final String cls,final String style){
		tago("span");
		if(!isempty(cls))attr("class",cls);
		if(!isempty(style))attr("style",style);
		return tagoe();
	}
	public xwriter nbsp(){return p("&nbsp;");}
	private final OutputStream os;
	private static String encquot(final String text){if(text==null)return"";return text.replaceAll("\"","&quot;");}
	public xwriter spaned(final a e){
		tago("span").attrdef(e).attr("contenteditable").p(" onkeydown=\"$b(this)\" spellcheck=false");
//		p(" oncopy=\"var e=event;e.preventDefault();var r=window.getSelection();e.clipboardData.setData('text/plain',r);\"");
		p(" onpaste=\"var e=event;e.preventDefault();var t=e.clipboardData.getData('text/plain');document.execCommand('inserttext',false,t);\"");
		tagoe();
		try{e.to(new osltgt(os));}catch(Throwable t){throw new Error(t);}
		return span_();
	}
	public xwriter p_data_size(final long i){
		long x=i;
		final long megs=(x>>20);
		if(megs>0){
			x-=(megs<<20);
			p(megs).p("m");
			return this;
		}
		final long kilos=(x>>10);
		if(kilos>0){
			x-=(kilos<<10);
			p(kilos).p("k");
			return this;
		}
		if(x>0)p(x);
		return this;
	}
}
