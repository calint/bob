package b;

import static b.b.isempty;
import static b.b.tobytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class xwriter {
	public static String enc_js_in_attr(final String text) {
		if (text == null)
			return "";
		return text.replace("'", "\\'").replace("\"", "&quot;");
	}

	public static String enc_js_str(final String text) {
		if (text == null)
			return "";
		return text.replace("'", "\\'");
	}

	public static String enc_quot(final String text) {
		if (text == null)
			return "";
		return text.replace("\"", "&quot;");
	}

	private final OutputStream os;
	private boolean xreload_requested = false; // reload page races with element serialization to db
	private boolean is_xu_open;

	public xwriter() {
		os = new ByteArrayOutputStream();
	}

	public xwriter(final OutputStream os) {
		this.os = os;
	}

	public xwriter a(final String href) {
		return tago("a").attr("href", enc_quot(href)).tagoe();
	}

	public xwriter a(final String href, final String html) {
		return a(href).p(html).a_();
	}

	public xwriter a_() {
		return tage("a");
	}

	public xwriter attr(final String name) {
		return p(" ").p(name);
	}

	public xwriter attr(final String name, final int value) {
		return p(" ").p(name).p("=").p(value);
	}

	public xwriter attr(final String name, final String value) {
		return p(" ").p(name).p("=\"").p(enc_quot(value)).p("\"");
	}

	public xwriter ax(final a e) {
		return ax(e, "", "::");
	}

	public xwriter ax(final a e, final String callback) {
		return ax(e, callback, callback);
	}

	public xwriter ax(final a e, final String callback, final String html) {
		p("<a href=\"javascript:");
		js_x(e, callback, true);
		p("\">").p(html).p("</a>");
		return this;
	}

	public xwriter bell() {
		return p('\07');
	}

	public xwriter br() {
		return tag("br");
	}

	public xwriter code() {
		return tag("code");
	}

	public xwriter code_() {
		return tage("code");
	}

	public xwriter css(final a e, final String style) {
		return p("#").p(e.id()).p("{").p(style).p("}");
	}

	public xwriter css(final a e, final String selector, final String style) {
		return css("#" + e.id() + " " + selector, style);
	}

	public xwriter css(final String selector, final String style) {
		return p(selector).p("{").p(style).p("}");
	}

	public xwriter cssfont(final String name, final String url) {
		return p("@font-face{font-family:").p(name).p(";src:url(").p(url).p(");}");
	}

	public xwriter default_attrs_for_element(final a e) {
		final String id = e.id();
		return attr("id", id);
	}

	public xwriter div_() {
		return tage("div");
	}

	/** Renders a complete div with HTML escaped element output. */
	public xwriter div(final a e, final String cls, final String style) {
		divo(e, cls, style);
		try {
			e.to(new osltgt(os));
		} catch (final Throwable t) {
			throw new Error(t);
		}
		return div_();
	}

	public xwriter div(final a e, final String cls) {
		return div(e, cls, null);
	}

	public xwriter div(final a e) {
		return div(e, null, null);
	}

	/** Renders a div tag with element content. */
	public xwriter div_html(final a e, final String cls, final String style) {
		return divh(e, cls, style);
	}

	public xwriter divh(final a e) {
		return divh(e, null, null);
	}

	public xwriter divh(final a e, final String cls) {
		return divh(e, cls, null);
	}

	public xwriter divh(final a e, final String cls, final String style) {
		tago("div").attr("id", e.id());
		if (!isempty(cls)) {
			attr("class", cls);
		}
		if (!isempty(style)) {
			attr("style", style);
		}
		tagoe();
		try {
			e.to(this);
		} catch (final Throwable t) {
			throw new Error(t);
		}
		return div_();
	}

	public xwriter divo() {
		return divo(null, null, null);
	}

	public xwriter divo(final a e) {
		return divo(e, null, null);
	}

	public xwriter divo(final a e, final String cls) {
		return divo(e, cls, null);
	}

	public xwriter divo(final a e, final String cls, final String style) {
		divot(e, cls, style);
		return tagoe();
	}

	public xwriter divo(final String cls) {
		return divo(null, cls, null);
	}

	public xwriter divo(final String cls, final String style) {
		return divo(null, cls, style);
	}

	/**
	 * Opens a div tag so that other attributes can be added. Close with tagoe().
	 */
	public xwriter divot(final a e, final String cls, final String style) {
		tago("div");
		if (e != null) {
			attr("id", e.id());
		}
		if (!isempty(cls)) {
			attr("class", cls);
		}
		if (!isempty(style)) {
			attr("style", style);
		}
		return this;
	}

	public xwriter enter() {
		return p('\r');
	}

	/**
	 * Called before closing being the last script dont. Used to avoid racing
	 * between DbTransaction.commit() and reloading the page.
	 */
	public void finish() {
		if (xreload_requested) {
			pl("location.href=location.href");
		}
		xreload_requested = false;
	}

	public xwriter flush() {
		try {
			os.flush();
			return this;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public xwriter focus(final a e) {
		return script().p("$f('").p(e.id()).p("')").script_();
	}

	public xwriter hr() {
		return tag("hr");
	}

//	/** Closes inline script tag. */
//	public xwriter inline_js_close(){
//		return tage("is");
//	}
//	/** Opens a tag for inline script that is executed on the client side. The script is collected in a list from the response message and executed after the field has been set. */
//	public xwriter inline_js_open(){
//		return tag("is");
//	}
	// ? review this
	public xwriter inp(final a e, final String type, final String cls, final String style, final String default_value,
			final a on_enter_callback_elem, final String on_enter_callback, final a on_change_callback_elem,
			final String on_change_callback) {
		final String value = e.is_empty() ? default_value : e.str();
		tago("input").attr("value", value).default_attrs_for_element(e);
		if (!isempty(type)) {
			attr("type", type);
		}
		if (!isempty(cls)) {
			attr("class", cls);
		}
		if (!isempty(style)) {
			attr("style", style);
		}

		if (on_enter_callback_elem != null) {
			final StringBuilder sb = new StringBuilder(64);
			sb.append("return $r(event,this,'");
			sb.append(on_enter_callback_elem.id());
			if (!isempty(on_enter_callback)) {
				sb.append(' ').append(enc_js_in_attr(on_enter_callback));
			}
			sb.append("')");
			attr("onkeypress", sb.toString());
		}
		final StringBuilder sb = new StringBuilder(64);
		if ("checkbox".equals(type)) {
			if ("1".equals(value)) {
				attr("checked");
			}
			sb.append("this.value=this.checked?'1':'0';$b(this)");
			if (on_change_callback_elem != null) {
				sb.append(";$x('");
				sb.append(on_change_callback_elem.id());
				if (!isempty(on_change_callback)) {
					sb.append(' ').append(on_change_callback);
				}
				sb.append("')");
			}
		} else {
			sb.append("$b(this)");
			if (on_change_callback_elem != null) {
				sb.append(";$x('");
				sb.append(on_change_callback_elem.id());
				if (!isempty(on_change_callback)) {
					sb.append(' ');
					sb.append(enc_js_in_attr(on_change_callback));
				}
				sb.append("')");
			}
		}
		attr("oninput", sb.toString());
		return tagoe();// ? <input hidden value="false">
	}

	/**
	 * Input field that callbacks an element while typing.
	 *
	 * @param e             the element.
	 * @param cls           style class.
	 * @param callback_elem element to do callback on at change.
	 * @param callback      callback argument. First word is the method and the rest
	 *                      of the string is the parameter(s).
	 */
	public xwriter inpax(final a e, final String cls, final a callback_elem, final String callback) {
		tago("input").attr("value", e.toString()).default_attrs_for_element(e).attr("type", "text");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		attr("onfocus", "this.setSelectionRange(this.value.length,this.value.length)");
		final StringBuilder sb = new StringBuilder();
		sb.append(callback_elem.id());
		if (!isempty(callback)) {
			sb.append(" ").append(enc_js_in_attr(callback));
		}
		final String sbs = sb.toString();
		attr("oninput", "$b(this);$x('" + sbs + "');return true;");
		attr("onkeypress", "return $r(event,this,'" + callback_elem.id() + " sel')");
		return tagoe();
	}

	public xwriter inpax(final a e, final String cls, final a on_change_callback_elem, final String on_change_callback,
			final String on_enter_callback) {
		return inp(e, null, cls, null, null, on_change_callback_elem, on_enter_callback, on_change_callback_elem,
				on_change_callback);
	}

	public xwriter inpflt(final a e) {
		return tago("input").attr("value", e.toString()).default_attrs_for_element(e).attr("type", "text")
				.attr("class", "nbr").attr("size", 5).attr("oninput", "$b(this)").tagoe();
	}

	// public xwriter inp_color(final a e){
//		return inp(e,"color",null,null,null,null,null,null,null);
//	}
	public xwriter inpint(final a e) {
		return tago("input").attr("value", e.toString()).default_attrs_for_element(e).attr("type", "text")
				.attr("class", "nbr").attr("size", 5).attr("oninput", "$b(this)").tagoe();
	}

	public xwriter inplng(final a e) {
		return inpint(e);
	}

	public xwriter inptxt(final a e) {
		return inp(e, null, null, null, null, null, null, null, null);
	}

	public xwriter inptxt(final a e, final String cls) {
		return inp(e, null, cls, null, null, null, null, null, null);
	}

	public xwriter inptxt(final a e, final a callback_elem_on_enter) {
		return inp(e, null, null, null, null, callback_elem_on_enter, null, null, null);
	}

	public xwriter inptxt(final a e, final a callback_elem_on_enter, final String callback) {
		return inp(e, null, null, null, null, callback_elem_on_enter, callback, null, null);
	}

	public xwriter inptxt(final a e, final String cls, final a callback_elem_on_enter, final String callback) {
		return inp(e, null, cls, null, null, callback_elem_on_enter, callback, null, null);
	}

	public xwriter inptxt(final a e, final String cls, final String default_value, final a callback_elem_on_enter,
			final String callback) {
		return inp(e, null, cls, null, default_value, callback_elem_on_enter, callback, null, null);
	}

	public xwriter inptxtarea(final a e) {
		return inptxtarea(e, null);
	}

	/** Input text area. */
	public xwriter inptxtarea(final a e, final String cls) {
		tago("textarea").default_attrs_for_element(e).attr("onchange", "$b(this)").attr("onkeydown", "$b(this)");

		if (!isempty(cls)) {
			attr("class", cls);
		}
		attr("wrap", "off").attr("spellcheck", "false").tagoe();
		try {
			e.to(new osltgt(outputstream()));
		} catch (final Throwable t) {
			b.log(t);
			p(b.stacktrace(t));
		}
		return tage("textarea");
	}

//	/** Alias for inline_js_close() */
//	public xwriter is_(){
//		return inline_js_close();
//	}
//	/** Alias for inline_js_open() */
//	public xwriter is(){
//		return inline_js_open();
//	}
	public xwriter js_x(final a e, final boolean encode_for_attribute) {
		return js_x(e, null, encode_for_attribute);
	}

	public xwriter js_x(final a e, final String callback) {
		return js_x(e, callback, false);
	}

	/**
	 * Renders script for a callback.
	 *
	 * @param e                    element to call
	 * @param callback             first word is method name and remaining string is
	 *                             the parameter.
	 * @param encode_for_attribute true to encode param for script in HTML
	 *                             attribute.
	 */
	public xwriter js_x(final a e, final String callback, final boolean encode_for_attribute) {
		p("$x('").p(e.id());
		if (!isempty(callback)) {
			p(" ");
			if (encode_for_attribute) {
				p(enc_js_in_attr(callback));
			} else {
				p(enc_js_str(callback));
			}
		}
		p("');");
		return this;
	}

	@SuppressWarnings("resource") // osjsstr does not need to do any cleanup
	public xwriter jsstr(final String s) {
		try {
			new osjsstr(os).write(tobytes(s));
			return this;
		} catch (final IOException e) {
			throw new Error(e);
		}
	}

	public xwriter li() {
		return li(null);
	}

	public xwriter li(final String cls) {
		if (isempty(cls))
			return li();
		return tago("li").attr("class", cls).tagoe();
	}

	public xwriter nbsp() {
		return p("&nbsp;");
	}

	public xwriter nl() {
		return p('\n');
	}

	public xwriter nl(final int number_of_newlines) {
		for (int i = 0; i < number_of_newlines; i++) {
			p('\n');
		}
		return this;
	}

	public xwriter ol() {
		return tag("ol");
	}

	public xwriter ol_() {
		return tage("ol");
	}

	public OutputStream outputstream() {
		return os;
	}

	public xwriter p(final a e) {
		return p(e.str());
	}

	public xwriter p(final byte n) {
		return p(Byte.toString(n));
	}

	public xwriter p(final char n) {
		return p(Character.toString(n));
	}

	public xwriter p(final CharSequence cs) {
		return p(cs.toString());
	}

	public xwriter p(final double n) {
		return p(Double.toString(n));
	}

	public xwriter p(final float n) {
		return p(Float.toString(n));
	}

	public xwriter p(final int n) {
		return p(Integer.toString(n));
	}

	public xwriter p(final long n) {
		return p(Long.toString(n));
	}

	public xwriter p(final String s) {
		if (s == null)
			return this;
		try {
			os.write(tobytes(s));
		} catch (final IOException e) {
			throw new Error(e);
		}
		return this;
	}

	public xwriter p(final boolean n) {
		return p(Boolean.toString(n));
	}

	public xwriter pl() {
		return nl();
	}

	public xwriter pl(final String s) {
		return p(s).nl();
	}

	public xwriter pre() {
		return pre(null);
	}

	public xwriter pre(final String cls) {
		tago("pre");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		return tagoe();
	}

	public xwriter pre_() {
		return tage("pre");
	}

	public xwriter r(final a e) throws Throwable {
		return render(e);
	}

	public xwriter render(final a e) throws Throwable {
		if (e == null)
			return this;
		e.to(this);
		return this;
	}

	public xwriter script() {
		return tag("script");
	}

	public xwriter script_() {
		return tage("script");
	}

	public xwriter span(final a e) {
		return span(e, null, null);
	}

	public xwriter span(final a e, final String cls) {
		return span(e, cls, null);
	}

	/** Renders a complete span with HTML escaped element output. */
	public xwriter span(final a e, final String cls, final String style) {
		spano(e, cls, style);
		try {
			e.to(new osltgt(os));
		} catch (final Throwable t) {
			throw new Error(t);
		}
		return span_();
	}

	/** Closes span tag. */
	public xwriter span_() {
		return tage("span");
	}

	/** Renders a complete span with unescaped value. */
	public xwriter span_html(final a e, final String cls, final String style) {
		spano(e, cls, style);
		try {
			e.to(this);
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}
		return span_();
	}

	public xwriter spanh(final a e) {
		return span_html(e, null, null);
	}

	public xwriter spanh(final a e, final String cls) {
		return span_html(e, cls, null);
	}

	/** Alias for span_html(...). */
	public xwriter spanh(final a e, final String cls, final String style) {
		return span_html(e, cls, style);
	}

	public xwriter spano(final a e) {
		return spano(e, null, null);
	}

	public xwriter spano(final a e, final String cls) {
		return spano(e, cls, null);
	}

	/** Renders the span tag. Must be closed with span_(). */
	public xwriter spano(final a e, final String cls, final String style) {
		spanot(e, cls, style);
		return tagoe();
	}

//	public xwriter spanot(final a e){
//		return spanot(e,null,null);
//	}
//
//	public xwriter spanot(final a e,final String cls){
//		return spanot(e,cls,null);
//	}

	/**
	 * Opens a span tag with id, class and style allowing the appending of
	 * additional attributes. Must be closed with tagoe().
	 *
	 * @param e     the element
	 * @param cls   the class attribute
	 * @param style the style attribute
	 */
	public xwriter spanot(final a e, final String cls, final String style) {
		tago("span").attr("id", e.id());
		if (!isempty(cls)) {
			attr("class", cls);
		}
		if (!isempty(style)) {
			attr("style", style);
		}
		return this;
	}

	public xwriter spc() {
		return p(' ');
	}

	public xwriter style() {
		return p("<style>");
	}

	public xwriter style_() {
		return tage("style");
	}

	public xwriter tab() {
		return p('\t');
	}

	public xwriter table() {
		return table(null, null);
	}

	public xwriter table(final String cls) {
		return table(cls, null);
	}

	public xwriter table(final String cls, final String style) {
		tago("table");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		if (!isempty(style)) {
			attr("style", style);
		}
		tagoe();
		return this;
	}

	public xwriter table_() {
		return tage("table");
	}

	public xwriter tag(final String name) {
		return p("<").p(name).p(">");
	}

	/** End tag. Example tage("a") outputs </a> */
	public xwriter tage(final String name) {
		return p("</").p(name).p(">");
	}

	// public xwriter tag(final String name,final String id){
//		return p("<").p(name).p(" id=").p(id).p(">");
//	}
	/** Opens a tag. Example tago("a") outputs "<a" */
	public xwriter tago(final String name) {
		return p("<").p(name);
	}

	/** Outputs '>'. Called after a tago(...) to close the tag. */
	public xwriter tagoe() {
		return p(">");
	}

	public xwriter td() {
		return td(null, null);
	}

	public xwriter td(final int colspan) {
		return p("<td colspan=").p(colspan).p(">");
	}

	public xwriter td(final String cls) {
		return td(cls, null);
	}

	public xwriter td(final String cls, final String style) {
		tago("td");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		if (!isempty(style)) {
			attr("style", style);
		}
		return tagoe();
	}

	public xwriter td_() {
		return tage("td");
	}

	public xwriter th() {
		return th(null);
	}

	public xwriter th(final int colspan) {
		return tago("th").attr("colspan", colspan).tagoe();
	}

	public xwriter th(final String cls) {
		tago("th");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		return tagoe();
	}

	public xwriter th_() {
		return tage("th");
	}

	public xwriter title(final String title) {
		return tag("title").p(title).tage("title");
	}

	@Override
	public String toString() {
		return os.toString();
	}

	public xwriter tr() {
		return tr(null);
	}

	public xwriter tr(final String cls) {
		tago("tr");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		return tagoe();
	}

	public xwriter tr_() {
		return tage("tr");
	}

	public xwriter ul() {
		return ul(null);
	}

	public xwriter ul(final String cls) {
		tago("ul");
		if (!isempty(cls)) {
			attr("class", cls);
		}
		return tagoe();
	}

	public xwriter ul_() {
		return tage("ul");
	}

	public xwriter xalert(final String s) {
		return p("ui.alert('").jsstr(s).pl("');");
	}

	public xwriter xfocus(final a e) {
		return p("$f('").p(e.id()).pl("');");
	}

	public xwriter xfocus(final String id) {
		return p("$f('").p(id).pl("');");
	}

	/** Prints to element content. */
	public xwriter xp(final a e, final String s) {
		return p("$p('").p(e.id()).p("','").jsstr(s).pl("');");
	}

	public xwriter xreload() {
		xreload_requested = true;
		return this;
	}

	public xwriter xtitle(final String s) {
		return p("$t('").jsstr(s).pl("');");
	}

	/**
	 * Updates value attribute of the HTML rendered by element. If HTML element does
	 * not have value attribute it replaces the inner HTML.
	 */
	public xwriter xuv(final a e) throws Throwable {
		return p("$s('").p(e.id()).p("','").p(enc_js_str(e.str())).pl("');");
	}

	/** Updates inner HTML of element e. */
	public xwriter xu(final a e) throws Throwable {
		return xu(e, false);
	}

	/** Updates inner HTML of elements e... */
	public xwriter xu(final a... es) throws Throwable {
		for (final a e : es) {
			e.to(xub(e, true, false));
			xube();
		}
		return this;
	}

	/**
	 * Updates inner HTML of element.
	 *
	 * @param e       element
	 * @param escltgt true to escape lt and gt.
	 */
	public xwriter xu(final a e, final boolean escltgt) throws Throwable {
		e.to(xub(e, true, escltgt));
		return xube();
	}

	public xwriter xu(final a e, final String s) {
		return xu(e.id(), s);
	}

	public xwriter xu(final String id, final String s) {
		return p("$s('").p(id).p("','").jsstr(s).pl("');");
	}

	/**
	 * Update inner or outer element.
	 * 
	 * @param e       element
	 * @param inner   true if update inner HTML, false if update outter element.
	 * @param escltgt true to escape lt and gt.
	 * @return xwriter xwriter that will generate a script set call. Will update
	 *         element inner or outer HTML code. Must be closed with xube().
	 */
	public xwriter xub(final a e, final boolean inner, final boolean escltgt) {
		is_xu_open = true;
		p("$").p(inner ? "s" : "o").p("('").p(e.id()).p("','");
		return new xwriter(new osjsstr(escltgt ? new osltgt(os) : os));
	}

	public xwriter closeUpdateIfOpen() {
		if (!is_xu_open)
			return this;
		is_xu_open = false;
		return xube();
	}

	/** Completes an xub(...) operation. */
	public xwriter xube() {
		is_xu_open = false;
		return pl("');");
	}

	/** Updates element outer HTML. */
	public xwriter xuo(final a e) throws Throwable {
		e.to(xub(e, false, false));
		return xube();
	}

	/** Scrolls page to top */
	public xwriter xscrollToTop() {
		return pl("ui.scrollToTop();");
	}
}
