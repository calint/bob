package b;
import static b.b.isempty;
import static b.b.tobytes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
public final class xwriter{
	private final OutputStream os;
	public xwriter a(final String href){
		return tago("a").attr("href",href).tagoe();
	}
	public xwriter a_(){
		return tage("a");
	}
	public xwriter a(final String href,final String html){
		return a(href).p(html).a_();
	}
	public xwriter ax(final a e,final String func,final String param,final String html,final String accesskey){
		final String wid=e.id();
		p("<a");
		if(accesskey!=null)
			spc().p("accesskey=").p(accesskey).attr("title",accesskey);
		p(" href=\"javascript:").axjs(wid,func,param).p("\">").p(html).p("</a>");
		return this;
	}
	public xwriter ajx(final a e,final String args){
		return tago("a").attr("href","javascript:$x('"+e.id()+" "+args+"')").attr("id",e.id()).tagoe();
	}
	public xwriter ajx(final a e){
		return tago("a").attr("href","javascript:$x('"+e.id()+"')").tagoe();
	}
	public xwriter ajx_(){
		return tage("a");
	}

//	public xwriter ph_div(final a e){return tago("div").attr("id",e.id()).tagoe().tage("div");}
	public xwriter focus(final a e){
		return script().p("$f('").p(e.id()).p("')").script_();
	}
	public xwriter script(){
		return tag("script");
	}
	public xwriter script_(){
		return tage("script");
	}
//	public xwriter spanx(final a e){
//		return spanx(e,null);
//	}
//	public xwriter spanx(final a e,final String style){
//		tago("span").attr("id",e.id());
//		if(style!=null)
//			attr("style",style);
//		return tagoe().tage("span");
//	}
//	public xwriter span(final a e){
//		return span(e,null);
//	}
//	public xwriter span(final a e,final String style){
//		tago("span").attr("id",e.id());
//		if(style!=null)
//			attr("style",style);
//		tagoe();
//		try{
//			e.to(new osltgt(os));
//		}catch(Throwable t){
//			throw new Error(t);
//		}
//		return span_();
//	}
//	public xwriter rend(final a e)throws Throwable{if(e==null)return this;e.to(this);return this;}

	public xwriter flush(){
		try{
			os.flush();
			return this;
		}catch(final IOException e){
			throw new Error(e);
		}
	}
	public String toString(){
		return os.toString();
	}
	public xwriter hr(){
		return tag("hr");
	}
	public xwriter spc(){
		return p(' ');
	}
	public xwriter tab(){
		return p('\t');
	}
	public xwriter enter(){
		return p('\r');
	}
	public xwriter bell(){
		return p('\07');
	}
	public xwriter inpax(final a e,final String stylecls,final a ax,final String onchangeaxp,final String onselectaxp){
		tago("input").attr("value",e.toString()).default_attrs_for_element(e).attr("type","text");
		if(!isempty(stylecls))
			attr("class",stylecls);
		final String eid=ax.id();
		attr("onfocus","this.setSelectionRange(this.value.length,this.value.length)");
		attr("oninput","$b(this);$x('"+eid+" "+onchangeaxp+"');return true;");
		if(onselectaxp!=null)
			attr("onkeypress","if(!event)event=window.event;if(event.keyCode!=13)return true;$x('"+eid+" "+onselectaxp+"');return false;");
		return tagoe();
	}
	public xwriter inputax(final a e){
		return inpax(e,null,e.parent(),null);
	}
	public xwriter output_holder(final a e){
		return tago("output").attr("id",e.id()).tagoe().tage("output");
	}// ? value
	public xwriter spc(final int n){
		for(int i=0;i<n;i++)
			spc();
		return this;
	}
	public xwriter ul(final String cls){
		return tago("ul").attr("class",cls).tagoe();
	}
	public xwriter cssfont(final String name,final String url){
		return p("@font-face{font-family:").p(name).p(";src:url(").p(url).p(");}");
	}
	@SuppressWarnings("resource") // osjsstr does not need to do any cleanup
	public xwriter jsstr(final String s){
		try{
			new osjsstr(os).write(tobytes(s));
			return this;
		}catch(final IOException e){
			throw new Error(e);
		}
	}
	public xwriter xu(final String id,final String s){
		return p("$s('").p(id).p("','").jsstr(s).pl("');");
	}
	public xwriter xu(final a e,final String s){
		return xu(e.id(),s);
	}
	public xwriter xinterval(final a e,final String ax,final int ms){
		return p("setInterval(\"$x('").p(e.id()).p(" ").p(ax).p("')\",").p(ms).pl(");");
	}
	private xwriter xhide(final a e,final boolean hide){
		// ? bug style block display:inherit
		return p("$('").p(e.id()).p("').style.display='").p(hide?"none":"inline").pl("';");
	}
	public xwriter xhide(final a e){
		return xhide(e,true);
	}
	public xwriter xshow(final a e){
		return xhide(e,false);
	}
	public xwriter xalert(final String s){
		return p("ui.alert('").jsstr(s).pl("');");
	}

	// reload page races with element serialization to db
	private boolean xreload_requested=false;
	public xwriter xreload(){
		xreload_requested=true;
		return this;
	}
	// called after element has been written to db
	public void finish(){
		if(xreload_requested)
//			pl("location.reload(true);");
			pl("location.href=location.href");
		xreload_requested=false;
	}
	public xwriter xfocus(final a e){
		return p("$f('").p(e.id()).pl("');");
	}
	public xwriter xfocus(final String id){
		return p("$f('").p(id).pl("');");
	}
	public xwriter xtitle(final String s){
		return p("$t('").jsstr(b.isempty(s,"")).pl("');");
	}
	public xwriter xp(final a e,final String s){
		return p("$p('").p(e.id()).p("','").jsstr(s).pl("');");
	}
	public xwriter el(){
		return p("<div style=display:inline>");
	}
//	public xwriter el(){return p("<span>");}
	public xwriter el(final String style){
		return p("<div style=\"display:inline;").p(style).p("\">");
	}
//	public xwriter el(final String style){return p("<span style=\"").p(style).p("\">");}
	public xwriter el(final a e){
		return p("<div style=display:inline id=").p(e.id()).p(">");
	}
//	public xwriter el(final a e){return p("<span id=").p(e.id()).p(">");}
	public xwriter el(final a e,final String style){
		return p("<div id=").p(e.id()).p(" style=\"display:inline;").p(style).p("\">");
	}
//	public xwriter el(final a e,final String style){return p("<span id=").p(e.id()).p(" style=\"").p(style).p("\">");}
	public xwriter el_(){
		return p("</div>");
	}
//	public xwriter el_(){return p("</span>");}
	public xwriter r(final a e) throws Throwable{
		if(e==null)
			return this;
		e.to(this);
		return this;
	}
	public xwriter rel(final a e) throws Throwable{
		if(e==null)
			return this;
		el(e);
		e.to(this);
		return el_();
	}
	public xwriter rdiv(final a e) throws Throwable{
		if(e==null)
			return this;
		divo(e);
		e.to(this);
		return div_();
	}
	public xwriter style(final a e,final String style){
		return style().css(e,style).style_();
	}
	public xwriter style(final a e,final String selector,final String style){
		return style().css(e,selector,style).style_();
	}
	public xwriter style(final String selector,final String style){
		return style().css(selector,style).style_();
	}
	public xwriter ax(final a e){
		return ax(e,"","::");
	}
	public xwriter dived(final a e,final a axe,final String axp,final String style) throws Throwable{
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
	public xwriter dived(final a e,final a axe,final String axp) throws Throwable{
		return dived(e,axe,axp,null);
	}
	public xwriter dived(final a e) throws Throwable{
		return dived(e,null,null,null);
	}
	public xwriter css(final a e,final String selector,final String style){
		return css("#"+e.id()+" "+selector,style);
	}
	public xwriter xlocation(final String uri){
		return p("location='").p(uri).pl("';");
	}
//	public xwriter span(final String style){
//		return span("",style);
//	}
//	public xwriter span(final String cls,final String style){
//		tago("span");
//		if(!isempty(cls))
//			attr("class",cls);
//		if(!isempty(style))
//			attr("style",style);
//		return tagoe();
//	}
	public xwriter spaned(final a e){
		tago("span").default_attrs_for_element(e).attr("contenteditable").p(" onkeydown=\"$b(this)\" spellcheck=false");
//		p(" oncopy=\"var e=event;e.preventDefault();var r=window.getSelection();e.clipboardData.setData('text/plain',r);\"");
		p(" onpaste=\"var e=event;e.preventDefault();var t=e.clipboardData.getData('text/plain');document.execCommand('inserttext',false,t);\"");
		tagoe();
		try{
			e.to(new osltgt(os));
		}catch(Throwable t){
			throw new Error(t);
		}
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
		if(x>0)
			p(x);
		return this;
	}

	public xwriter axjs(final String eid,final String func,final String param){
		p("$x('").p(eid);
		if(!isempty(func))
			p(" ").p(func);
		if(!isempty(param))
			p(" ").p(param);
		return p("')");
	}
	public xwriter ax(final a e,final String callback,final String param,final String html){
		final String wid=e.id();
		p("<a href=\"javascript:").axjs(wid,callback,param).p("\">").p(html).p("</a>");
		return this;
	}
	// -----------------------------------------------------------------------------------
	// -------------- reviewed for use with bob... encoding of string in string etc
	// -----------------------------------------------------------------------------------
	public xwriter(final OutputStream os){
		this.os=os;
	}
	public xwriter(){
		os=new ByteArrayOutputStream();
	}
	public OutputStream outputstream(){
		return os;
	}
	public xwriter p(final String s){
		if(s==null)
			return this;
		try{
			os.write(tobytes(s));
		}catch(final IOException e){
			throw new Error(e);
		}
		return this;
	}
	public xwriter nl(){
		return p('\n');
	}
	public xwriter nl(final int number_of_newlines){
		for(int i=0;i<number_of_newlines;i++)
			p('\n');
		return this;
	}
	public xwriter p(final byte n){
		return p(Byte.toString(n));
	}
	public xwriter p(final char n){
		return p(Character.toString(n));
	}
	public xwriter p(final int n){
		return p(Integer.toString(n));
	}
	public xwriter p(final float n){
		return p(Float.toString(n));
	}
	public xwriter p(final long n){
		return p(Long.toString(n));
	}
	public xwriter p(final double n){
		return p(Double.toString(n));
	}
	public xwriter p(final CharSequence cs){
		return p(cs.toString());
	}
	public xwriter p(final a e){
		return p(e.str());
	}
	public xwriter pl(final String s){
		return p(s).nl();
	}
	public xwriter pl(){
		return nl();
	}
	public xwriter br(){
		return tag("br");
	}
	public xwriter nbsp(){
		return p("&nbsp;");
	}
	public xwriter tag(final String name){
		return p("<").p(name).p(">");
	}
//	public xwriter tag(final String name,final String id){
//		return p("<").p(name).p(" id=").p(id).p(">");
//	}
	/** Opens a tag. Example tago("a") outputs "<a" */
	public xwriter tago(final String name){
		return p("<").p(name);
	}
	public xwriter default_attrs_for_element(final a e){
		final String id=e.id();
		return attr("id",id);
	}
	public xwriter attr(final String name,final int value){
		return p(" ").p(name).p("=").p(value);
	}
	public xwriter attr(final String name,final String value){
		return p(" ").p(name).p("=\"").p(enc_quot(value)).p("\"");
	}
	public xwriter attr(final String name){
		return p(" ").p(name);
	}
	/** Outputs '>'. Called after a tago(...) to close the tag. */
	public xwriter tagoe(){
		return p(">");
	}
	/** End tag. Example tage("a") outputs </a> */
	public xwriter tage(final String name){
		return p("</").p(name).p(">");
	}
	public xwriter title(final String title){
		return tag("title").p(title).tage("title");
	}
	public xwriter css(final String selector,final String style){
		return p(selector).p("{").p(style).p("}");
	}
	public xwriter css(final a e,final String style){
		return p("#").p(e.id()).p("{").p(style).p("}");
	}
	/** Opens a tag for inline script that is executed on the client side. The script is collected in a list from the response message and executed after the field has been set. */
	public xwriter inline_js_open(){
		return tag("is");
	}
	/** Alias for inline_js_open() */
	public xwriter iso(){
		return inline_js_open();
	}
	/** Closes inline script tag. */
	public xwriter inline_js_close(){
		return tage("is");
	}
	/** Alias for inline_js_close() */
	public xwriter isc(){
		return inline_js_close();
	}
	/**
	 * Opens a span tag with id, class and style allowing the appending of additional attributes. Must be closed with tagoe().
	 * 
	 * @param e     the element
	 * @param cls   the class attribute
	 * @param style the style attribute
	 */
	public xwriter spanot(final a e,String cls,String style){
		tago("span").attr("id",e.id());
		if(!isempty(cls))
			attr("class",cls);
		if(!isempty(style))
			attr("style",style);
		return this;
	}
	public xwriter spanot(final a e,String cls){
		return spanot(e,cls,null);
	}
	public xwriter spanot(final a e){
		return spanot(e,null,null);
	}
	/** Renders the span tag. Must be closed with span_(). */
	public xwriter spano(final a e,String cls,String style){
		spanot(e,cls,style);
		return tagoe();
	}
	public xwriter spano(final a e,String cls){
		return spano(e,cls,null);
	}
	public xwriter spano(final a e){
		return spano(e,null,null);
	}
	/** Renders a complete span with HTML escaped value. */
	public xwriter span(final a e,final String cls,final String style){
		spano(e,cls,style);
		try{
			e.to(new osltgt(os));
		}catch(Throwable t){
			throw new Error(t);
		}
		return span_();
	}
	public xwriter span(final a e,String cls){
		return span(e,cls,null);
	}
	public xwriter span(final a e){
		return span(e,null,null);
	}
	/** Renders a complete span with unescaped value. */
	public xwriter span_html(final a e,final String cls,final String style){
		spano(e,cls,style);
		try{
			e.to(os);
		}catch(Throwable t){
			throw new RuntimeException(t);
		}
		return span_();
	}
	/** Alias for span_html(...). */
	public xwriter spanh(final a e,final String cls,final String style){
		return span_html(e,cls,style);
	}
	public xwriter spanh(final a e,final String cls){
		return span_html(e,cls,null);
	}
	public xwriter spanh(final a e){
		return span_html(e,null,null);
	}
	/** Closes span tag. */
	public xwriter span_(){
		return tage("span");
	}
	public xwriter ax(final a e,final String callback,final String html){
		p("<a href=\"javascript:");
		js_x(e,callback,true);
		p("\">").p(html).p("</a>");
		return this;
	}
	public xwriter ax(final a e,final String callback){
		return ax(e,callback,callback);
	}
	public xwriter table(final String cls,final String style){
		tago("table");
		if(!isempty(cls))
			attr("class",cls);
		if(!isempty(style))
			attr("style",style);
		tagoe();
		return this;
	}
	public xwriter table(final String cls){
		return table(cls,null);
	}
	public xwriter table(){
		return table(null,null);
	}
	public xwriter tr(final String cls){
		tago("tr");
		if(!isempty(cls))
			attr("class",cls);
		return tagoe();
	}
	public xwriter tr(){
		return tr(null);
	}
	public xwriter tr_(){
		return tage("tr");
	}
	public xwriter td(final String cls,final String style){
		tago("td");
		if(!isempty(cls))
			attr("class",cls);
		if(!isempty(style))
			attr("style",style);
		return tagoe();
	}
	public xwriter td(final String cls){
		return td(cls,null);
	}
	public xwriter td(){
		return td(null,null);
	}
	public xwriter td_(){
		return tage("td");
	}
	public xwriter th(final String cls){
		tago("th");
		if(!isempty(cls))
			attr("class",cls);
		return tagoe();
	}
	public xwriter th(){
		return th(null);
	}
	public xwriter th(final int colspan){
		return tago("th").attr("colspan",colspan).tagoe();
	}
	public xwriter th_(){
		return tage("th");
	}
	public xwriter table_(){
		return tage("table");
	}
	public xwriter td(final int colspan){
		return p("<td colspan=").p(colspan).p(">");
	}
	public xwriter style(){
		return p("<style>");
	}
	public xwriter style_(){
		return tage("style");
	}
	public xwriter divot(final a e,final String cls,final String style){
		tago("div");
		if(e!=null)
			attr("id",e.id());
		if(!isempty(cls))
			attr("class",cls);
		if(!isempty(style))
			attr("style",style);
		return this;
	}
	public xwriter divo(final a e,final String cls,final String style){
		divot(e,cls,style);
		return tagoe();
	}
	public xwriter divo(final a e,final String cls){
		return divo(e,cls,null);
	}
	public xwriter divo(final a e){
		return divo(e,null,null);
	}
	public xwriter divo(final String cls,final String style){
		return divo(null,cls,style);
	}
	public xwriter divo(final String cls){
		return divo(null,cls,null);
	}
	public xwriter divo(){
		return divo(null,null,null);
	}
	public xwriter div_(){
		return tage("div");
	}
	/** Renders a div tag with element content. */
	public xwriter div_html(final a e,final String cls,final String style){
		tago("div").attr("id",e.id());
		if(!isempty(cls))
			attr("class",cls);
		if(!isempty(style))
			attr("style",style);
		tagoe();
		try{
			e.to(this);
		}catch(Throwable t){
			throw new Error(t);
		}
		return div_();
	}
	/** Alias for div_html(...). */
	public xwriter divh(final a e,final String cls,final String style){
		return div_html(e,cls,style);
	}
	public xwriter divh(final a e,final String cls){
		return divh(e,cls,null);
	}
	public xwriter divh(final a e){
		return divh(e,null,null);
	}
	public xwriter pre(final String cls){
		tago("pre");
		if(!isempty(cls))
			attr("class",cls);
		return tagoe();
	}
	public xwriter pre(){
		return pre(null);
	}
	public xwriter pre_(){
		return tage("pre");
	}
	// ? review this
	public xwriter inp(final a e,final String type,final String cls,final String style,final a on_enter_callback_elem,final String on_enter_callback,final String default_value,final a on_change_callback_elem,final String on_change_callback){
		final String value=default_value==null?e.toString():default_value;
		tago("input").attr("value",value).default_attrs_for_element(e);
		if(!isempty(type))
			attr("type",type);
		if(!isempty(cls))
			attr("class",cls);
		if(!isempty(style))
			attr("style",style);

		if(on_enter_callback_elem!=null){
			final StringBuilder sb=new StringBuilder(64);
			sb.append("return $r(event,this,'");
			if(on_enter_callback_elem!=null){
				sb.append(on_enter_callback_elem.id());
				if(!isempty(on_enter_callback)){
					sb.append(' ').append(enc_js_in_attr(on_enter_callback));
				}
			}
			sb.append("')");
			attr("onkeypress",sb.toString());
		}
		final StringBuilder sb=new StringBuilder(64);
		if("checkbox".equals(type)){
			if(value.equals(Boolean.TRUE.toString()))
				attr("checked","checked");
			sb.append("this.value=this.checked?'1':'0';$b(this)");
			if(on_change_callback_elem!=null){
				sb.append(";$x('");
				sb.append(on_change_callback_elem.id());
				if(!isempty(on_change_callback)) {
					sb.append(' ').append(on_change_callback);
				}
				sb.append("')");
			}
		}else{
			sb.append("$b(this)");
			if(on_change_callback_elem!=null){
				sb.append(";$x('");
				sb.append(on_change_callback_elem.id());
				if(!isempty(on_change_callback)){
					sb.append(' ');
					sb.append(enc_js_in_attr(on_change_callback));
				}
				sb.append("')");
			}
		}
		attr("oninput",sb.toString());
		return tagoe();// ? <input hidden value="false">
	}
	public xwriter inptxt(final a e){
		return inp(e,null,null,null,null,null,null,null,null);
	}
	public xwriter inptxt(final a e,final a callback_elem_on_enter){
		return inp(e,null,null,null,callback_elem_on_enter,null,null,null,null);
	}
	public xwriter inptxt(final a e,final a callback_elem_on_enter,final String callback){
		return inp(e,null,null,null,callback_elem_on_enter,callback,null,null,null);
	}
	public xwriter inptxt(final a e,final a callback_elem_on_enter,final String callback,final String cls){
		return inp(e,null,cls,null,callback_elem_on_enter,callback,null,null,null);
	}
	public xwriter inptxt(final a e,final a callback_elem_on_enter,final String callback,final String default_value,final String cls){
		return inp(e,null,cls,null,callback_elem_on_enter,callback,default_value,null,null);
	}
//	public xwriter inp_color(final a e){
//		return inp(e,"color",null,null,null,null,null,null,null);
//	}
	public xwriter inpint(final a e){
		return tago("input").attr("value",e.toString()).default_attrs_for_element(e).attr("type","text").attr("class","nbr").attr("size",5).attr("oninput","$b(this)").tagoe();
	}
	public xwriter inpflt(final a e){
		return tago("input").attr("value",e.toString()).default_attrs_for_element(e).attr("type","text").attr("class","nbr").attr("size",5).attr("oninput","$b(this)").tagoe();
	}
	public xwriter inplng(final a e){
		return inpint(e);
	}
	/**
	 * Input field that callbacks an element while typing.
	 * 
	 * @param e             the element.
	 * @param cls           style class.
	 * @param callback_elem element to do callback on at change.
	 * @param callback      callback argument. First word is the method and the rest of the string is the parameter(s).
	 */
	public xwriter inpax(final a e,final String cls,final a callback_elem,final String callback){
		tago("input").attr("value",e.toString()).default_attrs_for_element(e).attr("type","text");
		if(!isempty(cls))
			attr("class",cls);
		attr("onfocus","this.setSelectionRange(this.value.length,this.value.length)");
		final StringBuilder sb=new StringBuilder();
		sb.append(callback_elem.id());
		if(!isempty(callback))
			sb.append(" ").append(enc_js_in_attr(callback));
		final String sbs=sb.toString();
		attr("oninput","$b(this);$x('"+sbs+"');return true;");
		attr("onkeypress","return $r(event,this,'"+callback_elem.id()+" sel')");
		return tagoe();
	}
	/** Input text area. */
	public xwriter inptxtarea(final a e,final String cls){
		tago("textarea").default_attrs_for_element(e).attr("onchange","$b(this)").attr("onkeydown","$b(this)");

		if(!isempty(cls))
			attr("class",cls);
		attr("wrap","off").attr("spellcheck","false").tagoe();
		try{
			e.to(new osltgt(outputstream()));
		}catch(final Throwable t){
			b.log(t);
			p(b.stacktrace(t));
		}
		return tage("textarea");
	}
	public xwriter inptxtarea(final a e){
		return inptxtarea(e,null);
	}
	public xwriter ul(){
		return tag("ul");
	}
	public xwriter ul_(){
		return tage("ul");
	}
	public xwriter li(final String cls){
		if(isempty(cls))
			return li();
		return tago("li").attr("class",cls).tagoe();
	}
	public xwriter li(){
		return li(null);
	}
	public xwriter ol(){
		return tag("ol");
	}
	public xwriter ol_(){
		return tage("ol");
	}
	public xwriter code(){
		return tag("code");
	}
	public xwriter code_(){
		return tage("code");
	}
	/**
	 * @param e       element
	 * @param inner   true if update inner HTML, false if update outter element.
	 * @param escltgt true to escape lt and gt.
	 * @return xwriter xwriter that will generate a script set call. Will update element inner or outer HTML code. Must be closed with xube().
	 */
	public xwriter xub(final a e,final boolean inner,final boolean escltgt){
		p("$").p(inner?"s":"o").p("('").p(e.id()).p("','");
		return new xwriter(new osjsstr(escltgt?new osltgt(os):os));
	}
	/** Completes an xub(...) operation. */
	public xwriter xube(){
		return pl("');");
	}
	/**
	 * Updates inner HTML of element.
	 * 
	 * @param e       element
	 * @param escltgt true to escape lt and gt.
	 */
	public xwriter xu(final a e,final boolean escltgt) throws Throwable{
		e.to(xub(e,true,escltgt));
		return xube();
	}
	/** Updates inner HTML of element e. */
	public xwriter xu(final a e) throws Throwable{
		return xu(e,false);
	}
	/** Updates inner HTML of elements e... */
	public xwriter xu(final a...es) throws Throwable{
		for(a e:es){
			e.to(xub(e,true,false));
			xube();
		}
		return this;
	}
	/** Updates element outer HTML. */
	public xwriter xuo(final a e) throws Throwable{
		e.to(xub(e,false,false));
		return xube();
	}
	/**
	 * Renders script for a callback.
	 * 
	 * @param e                    element to call
	 * @param callback             first word is method name and remaining string is the parameter.
	 * @param encode_for_attribute true to encode param for script in HTML attribute.
	 */
	public xwriter js_x(a e,String callback,boolean encode_for_attribute){
		p("$x('").p(e.id());
		if(!isempty(callback)){
			p(" ");
			if(encode_for_attribute)
				p(enc_js_in_attr(callback));
			else
				p(enc_js_str(callback));
		}
		p("');");
		return this;
	}
	public xwriter js_x(a e,String callback){
		return js_x(e,callback,false);
	}
	public xwriter js_x(a e,boolean encode_for_attribute){
		return js_x(e,null,encode_for_attribute);
	}

	private static String enc_quot(final String text){
		if(text==null)
			return "";
		return text.replaceAll("\"","&quot;");
	}

	private static String enc_js_in_attr(final String text){
		if(text==null)
			return "";
		return text.replaceAll("'","\\\\'").replaceAll("\"","&quot;");
	}

	private static String enc_js_str(final String text){
		if(text==null)
			return "";
		return text.replaceAll("'","\\\\'");
	}

}
