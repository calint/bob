package b;
import static b.b.p;
import static b.b.pl;
import static b.b.stacktrace;
import static b.b.tobytes;
import static b.b.tostr;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import b.b.conf;
public final class req{
	b.op parse()throws Throwable{while(true){
		if(ba_rem==0){
			bb.clear();
			final int c=sockch.read(bb);
			if(c==0)return b.op.read;//? infloop
			if(c==-1){close();return b.op.noop;}
			thdwatch.input+=c;
			bb.flip();
			ba=bb.array();
			ba_pos=bb.position();
			ba_rem=bb.remaining();
			if(b.print_requests){
				final String s=new String(ba,ba_pos,ba_rem);
				p(s);
			}
		}
		while(ba_rem>0){switch(state){default:throw new Error();
			case state_nextreq:methodlen=0;state=state_method;
			case state_method:parse_method();break;
			case state_uri:parse_uri();break;
			case state_prot:parse_prot();break;
			case state_header_name:parse_header_name();
				if(state==state_transfer_buffers||state==state_transfer_file)
					return b.op.write;
				if(state==state_waiting_run_page||state==state_waiting_run_page_content){
					b.thread(this);
					return b.op.noop;
				}
				break;
			case state_header_value:parse_header_value();break;
			case state_content_read:parse_content_read();
				if(state==state_waiting_run_page_content){
					b.thread(this);
					return b.op.noop;
				}
				break;
			case state_content_upload:parse_content_upload();break;
		}}
		if(state==state_nextreq&&!is_connection_keepalive()){close();return b.op.noop;}
	}}
	private int methodlen;
	public static @conf int abuse_method_len=5;
	private void parse_method(){
		final int ba_pos_prev=ba_pos;
		while(ba_rem!=0){
			final byte b=ba[ba_pos++];ba_rem--;
			if(b==' '){state=state_uri;sb_path.setLength(0);urilen=0;break;}
		}
		methodlen+=(ba_pos-ba_pos_prev);
		if(methodlen>abuse_method_len){close();throw new Error("abusemethodlen"+methodlen);}
	}
	private int urilen;
	public static @conf int abuse_uri_len=512;
	private void parse_uri(){
		final int ba_pos_prev=ba_pos;
		while(ba_rem!=0){
			final byte b=ba[ba_pos++];ba_rem--;
			if(b==' '){state=state_prot;protlen=0;break;}
			if(b=='\n'){do_after_prot();break;}// ie: '. index.html\n' //todo allow white space in request line
			sb_path.append((char)b);
		}
		urilen+=(ba_pos-ba_pos_prev);
		if(urilen>abuse_uri_len)
			{close();throw new Error("abuseurilen"+urilen);}
	}
	private int protlen;
	public static @conf int abuse_prot_len=11;
	private void parse_prot()throws Throwable{
		final int ba_pos_prev=ba_pos;
		while(ba_rem!=0){
			final byte b=ba[ba_pos++];ba_rem--;
			if(b!='\n')continue;
			if(ba_pos>=3&&ba[ba_pos-3]=='1')connection_keep_alive=true;// cheapo to set keepalive for http/1.1\r\n
			else if(ba_pos>=2&&ba[ba_pos-2]=='1')connection_keep_alive=true;// cheapo to set keepalive for 1\n
			do_after_prot();
			break;
		}
		protlen+=(ba_pos-ba_pos_prev);
		if(protlen>abuse_prot_len){close();throw new Error("abuseprotlen"+protlen);}
	}
	private void do_after_prot(){
		thdwatch.reqs++;
		final String uriencoded=sb_path.toString().trim();
		final int i=uriencoded.indexOf('?');
		if(i==-1){
			path_s=b.urldecode(uriencoded);
			query_s="";
		}else{
			path_s=b.urldecode(uriencoded.substring(0,i));
			query_s=uriencoded.substring(i+1);
		}
		hdrs.clear();
		headernamelen=headerscount=0;
		state=state_header_name;
	}
	private int headernamelen;
	public static @conf int abuse_header_name_len=32;
	private void parse_header_name()throws Throwable{
		final int ba_pos_prev=ba_pos;
		while(ba_rem!=0){
			final byte b=ba[ba_pos++];ba_rem--;
			if(b==':'){state=state_header_value;headervaluelen=0;break;}
			else if(b=='\n'){do_after_header();return;}
			else{sb_header_name.append((char)b);}
		}
		headernamelen+=(ba_pos-ba_pos_prev);
		if(headernamelen>abuse_header_name_len){close();throw new Error("abuseheadernamelen"+headernamelen);}
	}
	private @conf int headervaluelen;
	public static @conf int abuse_header_value_len=256;
	private int headerscount;
	public static @conf int abuse_header_count=32;
	private void parse_header_value(){
		final int ba_pos_prev=ba_pos;
		while(ba_rem!=0){
			final byte b=ba[ba_pos++];ba_rem--;
			if(b=='\n'){
				hdrs.put(sb_header_name.toString().trim().toLowerCase(),sb_header_value.toString().trim());
				headerscount++;
				if(headerscount>abuse_header_count){close();throw new Error("abuseheaderscount"+headerscount);}
				sb_header_name.setLength(0);
				sb_header_value.setLength(0);
				headernamelen=0;
				state=state_header_name;
				break;
			}
			sb_header_value.append((char)b);
		}
		headervaluelen+=(ba_pos-ba_pos_prev);
		if(headervaluelen>abuse_header_value_len){close();throw new Error("abuseheadervaluelen"+headervaluelen);}
	}
	public static @conf long abuse_upload_len=16*b.G;
	public static @conf long abuse_content_len=1*b.M;
	private void do_after_header()throws Throwable{
//		assertaccess();
		final String ka=hdrs.get(hk_connection);if(ka!=null)connection_keep_alive=hv_keep_alive.equalsIgnoreCase(ka);
		content.clear();
		contentType=hdrs.get(hk_content_type);
		if(contentType!=null){
			if(contentType.startsWith("dir;")||contentType.equals("dir")){
				if(!b.enable_upload)throw new Error("uploadsdisabled");
				if(!decodecookie())throw new Error("nocookie");
				final String[]q=contentType.split(";");
				final String lastmod_s=q[1];
				final path p=b.path(b.sessions_dir).get(sesid).get(path_s);
				if(!p.exists())p.mkdirs();
				if(!p.isdir())throw new Error("isnotdir: "+p);
				final SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd--HH:mm:ss.SSS");
				try{p.lastmod(df.parse(lastmod_s).getTime());}catch(final ParseException e){throw new Error(e);}
				reply(h_http204,null,null,null);
				state=state_nextreq;
				return;
			}
			if(contentType.startsWith("file;")||contentType.equals("file")){
				if(!b.enable_upload)throw new Error("uploadsdisabled");
//				System.out.println(path_s);
				if(!decodecookie())throw new Error("nocookie");
//				final String contentLength_s=hdrs.get(hk_content_length);
				contentLength=Long.parseLong(hdrs.get(hk_content_length));
				if(contentLength>abuse_upload_len){close();throw new Error("abuseuploadlen"+contentLength);}
				final String[]q=contentType.split(";");
				upload_lastmod_s=q.length>1?q[1]:new SimpleDateFormat("yyyy-MM-dd--HH:mm:ss.SSS").format(new Date());
	//				final String time=q[1];
	//				final String size=q[2];
	//				final String md5=q[3];
	//				final String range=q[4];
				try{upload_path=b.path(b.sessions_dir).get(sesid).get(path_s);}catch(final Throwable t){close();throw t;}
				upload_channel=upload_path.filechannel();
				bb.position(ba_pos);
				state=state_content_upload;parse_content_upload();
				return;
			}
		}
		final String contentLength_s=hdrs.get(hk_content_length);
		if(contentLength_s!=null){
			if(!decodecookie())throw new Error("nocookie");
			contentLength=Long.parseLong(contentLength_s);
			if(contentLength>abuse_content_len){close();throw new Error("abusecontentlen"+contentLength);}
			bb_content=ByteBuffer.allocate((int)contentLength);
			state=state_content_read;parse_content_read();
			return;
		}
		//. firewall path
		try{pth=b.path(path_s);}catch(final Throwable t){
			reply(h_http404,null,null,tobytes(b.stacktrace(t)));
			close();
			throw t;
		}
		if(b.try_file&&try_file())return;
		if(b.try_rc&&try_rc())return;
		decodecookie();
		state=state_waiting_run_page;
		if(!b.cache_uris)return;
		if(hdrs.get("range")!=null)return;//? mk ranged response from cache
		if(ses==null)ses=session.all().get(sesid);
		if(ses==null)return;
		if(sesid!=null&&!sesid.equals(ses.id()))throw new Error("cookiechangeduringconnection");
		final a e=(a)ses.get(path_s);
		if(e==null)return;
		if(!(e instanceof cacheable))return;
		final cacheable ac=(cacheable)e;
		final String ifmodsince=hdrs.get(hk_if_modified_since);
		final String lastmod=ac.lastmod();
		if(ifmodsince!=null&&ifmodsince.equals(lastmod)){reply(h_http304,null,null,null);return;}
		String key=sb_path.toString();
		if(ac.cacheforeachuser())key=req.get().session().id()+"~"+key;
		final chdresp c=cacheu.get(key);
		if(c==null)return;
		if(!c.isvalid(System.currentTimeMillis()))return;
		reply(c);
		thdwatch.cacheu++;
	}
//	private void assertaccess(){
//		if(path_s.startsWith("/localhost")&&!sockch.socket().getRemoteSocketAddress().toString().startsWith("/0:0:0:0:0:0:0:1"))
//			throw new Error("may only be accessed from localhost");
//	}
	private boolean decodecookie(){
		sesid=hdrs.get(hk_cookie);
		if(sesid==null)return false;
		final String[]c1=sesid.split(";");
		for(String cc:c1){
			cc=cc.trim();
			if(cc.startsWith("i=")){
				final String[]c2=cc.split("i=");
				if(c2.length<2)throw new Error("invalidcookie: "+sesid);
				sesid=c2[1];
				sesid_set=false;
				return true;
			}
		}
		return false;
	}
	private static String mkcookieid(){
		final SimpleDateFormat sdf=new SimpleDateFormat("yyMMdd-hhmmss.SSS-");
		final StringBuilder sb=new StringBuilder(sdf.format(new Date()));
		final String alf="0123456789abcdef";
		for(int n=0;n<8;n++)sb.append(alf.charAt(b.rndint(0,alf.length())));
		return sb.toString();
	}
	private boolean try_file()throws Throwable{
		if(cachef!=null)if(try_cache())return true;
		if(!pth.exists())return false;
		if(pth.isdir()){
			pth=pth.get(b.default_directory_file);
			if(!pth.exists()||!pth.isfile())return false;
		}
		thdwatch.files++;
		final long lastmod_l=pth.lastmod();
		final String lastmod_s=b.tolastmodstr(lastmod_l);
		final String ifModSince=hdrs.get(hk_if_modified_since);
		if(ifModSince!=null&&ifModSince.equals(lastmod_s)){
			reply(h_http304,null,null,null);
			return true;
		}
		final long path_len=pth.size();
		final String range_s=hdrs.get(s_range);
		final ByteBuffer[]bb=new ByteBuffer[16];
		int i=0;
		final long range_from;
		if(range_s!=null){
			final String[]s=range_s.split(s_eq);
			final String[]ss=s[1].split(s_minus);
			range_from=Long.parseLong(ss[0]);
			bb[i++]=ByteBuffer.wrap(h_http206);
			bb[i++]=ByteBuffer.wrap(h_content_length);
			bb[i++]=ByteBuffer.wrap(Long.toString(path_len-range_from).getBytes());
			bb[i++]=ByteBuffer.wrap(hk_content_range);
			bb[i++]=ByteBuffer.wrap((s_bytes_+range_from+s_minus+path_len+s_slash+path_len).getBytes());
		}else{
			range_from=0;
			bb[i++]=ByteBuffer.wrap(h_http200);
			bb[i++]=ByteBuffer.wrap(h_content_length);
			bb[i++]=ByteBuffer.wrap(Long.toString(path_len).getBytes());
		}
		bb[i++]=ByteBuffer.wrap(h_last_modified);
		bb[i++]=ByteBuffer.wrap(lastmod_s.getBytes());
		bb[i++]=ByteBuffer.wrap(hkp_accept_ranges_byte);
		if(sesid_set){
			bb[i++]=ByteBuffer.wrap(hk_set_cookie);
			bb[i++]=ByteBuffer.wrap(sesid.getBytes());
			bb[i++]=ByteBuffer.wrap(hkv_cookie_append);
			sesid_set=false;
		}
		if(connection_keep_alive)
			bb[i++]=ByteBuffer.wrap(hkp_connection_keep_alive);
		bb[i++]=ByteBuffer.wrap(ba_crlf2);
		final long n=sendpacket(bb,i);
		thdwatch.output+=n;
		transfer_file_channel=pth.fileinputstream().getChannel();
		transfer_file_position=range_from;
		transfer_file_remaining=path_len-range_from;
		state=state_transfer_file;do_transfer_file();
		return true;
	}
	private boolean try_cache()throws Throwable{
		chdresp cachedresp;
		boolean validated=false;
		cachedresp=cachef.get(path_s);
		if(cachedresp==null){
			if(pth.isdir())pth=pth.get(b.default_directory_file);
			if(!pth.exists())return false;
			if(pth.size()<=b.cache_files_maxsize){
				cachedresp=new chdresp(pth);
				validated=true;
				cachef.put(path_s,cachedresp);
				thdwatch._cachef++;
			}
		}
		if(cachedresp==null)return false;
		if(!validated&&!cachedresp.validate(System.currentTimeMillis(),null)){
			synchronized(cachef){cachef.remove(path_s);}
			thdwatch._cachef--;
			return true;
		}
		reply(cachedresp);
		return true;
	}
	private boolean try_rc()throws Throwable{
		final String p=pth.name();//? path
		if(!b.resources_enable_any_path&&!b.resources_paths.contains(p))return false;
		final String rcpth;
		if(b.resources_paths.contains(p))
			rcpth="/"+req.class.getPackage().getName()+"/"+p;
		else if(b.resources_enable_any_path)
			rcpth="/"+b.webobjpkg.replace('.','/')+pth;
		else return false;
		
		final InputStream is=req.class.getResourceAsStream(rcpth);
		if(is==null)return false;
		final chdresp c=new chdresp(is);
		synchronized(cachef){cachef.put(path_s,c);}
		reply(c);
		return true;
	}
	private void reply(final chdresp c)throws Throwable{
		thdwatch.cachef++;
		final String ifModSince=hdrs.get(hk_if_modified_since);
		if(ifModSince!=null&&c.ifnotmodsince(ifModSince)){
			reply(h_http304,null,null,null);
			return;
		}
		if(b.allow_partial_content_from_cache){
			final String range=hdrs.get("range");
			if(range!=null){
				int i=0;
				final String from_to_in_bytes=range.split("=")[1];
				final String[]ft=from_to_in_bytes.split("-");
				final int range_from_byte=Integer.parseInt(tostr(ft[0],"0"));
				final int range_to_byte=Integer.parseInt(tostr(ft[1],"0"));
				final ByteBuffer[]bba=new ByteBuffer[sesid_set?10:7];
				bba[i++]=ByteBuffer.wrap(h_http206);
				bba[i++]=ByteBuffer.wrap(h_content_length);
				bba[i++]=ByteBuffer.wrap(Integer.toString(1+range_to_byte-range_from_byte).getBytes());
				bba[i++]=ByteBuffer.wrap(hk_content_range);
				bba[i++]=ByteBuffer.wrap((s_bytes_+range_from_byte+s_minus+range_to_byte+s_slash+c.content_length_in_bytes()).getBytes());
				if(sesid_set){
					sesid_set=false;// set cookie
					bba[i++]=ByteBuffer.wrap(hk_set_cookie);
					bba[i++]=ByteBuffer.wrap(sesid.getBytes());
					bba[i++]=ByteBuffer.wrap(hkv_cookie_append);
				}
				bba[i++]=ByteBuffer.wrap(ba_crlf2);
				final int d=c.data_position();
				bba[i++]=(ByteBuffer)c.byteBuffer().slice().position(d+range_from_byte).limit(d+range_to_byte+1);		
				transferbuffers(bba);
				return;
			}
		}
		if(!sesid_set){// no cookie to set
			transferbuffers(new ByteBuffer[]{c.byteBuffer().slice()});
			return;
		}
		sesid_set=false;// set cookie
		final ByteBuffer[]bba=new ByteBuffer[]{c.byteBuffer().slice(),ByteBuffer.wrap(hk_set_cookie),ByteBuffer.wrap(sesid.getBytes()),ByteBuffer.wrap(hkv_cookie_append),c.byteBuffer().slice()};
		bba[0].limit(c.additional_headers_insertion_position());
		bba[4].position(c.additional_headers_insertion_position());
		transferbuffers(bba);
	}
	private void reply(final byte[]firstline,final byte[]lastMod,final byte[]contentType,final byte[]content)throws Throwable{
		final ByteBuffer[]bb=new ByteBuffer[16];
		int bi=0;
		bb[bi++]=ByteBuffer.wrap(firstline);
		if(sesid_set){
			bb[bi++]=ByteBuffer.wrap(hk_set_cookie);
			bb[bi++]=ByteBuffer.wrap(sesid.getBytes());
			bb[bi++]=ByteBuffer.wrap(hkv_cookie_append);
			sesid_set=false;
		}
		if(lastMod!=null){
			bb[bi++]=ByteBuffer.wrap(h_last_modified);
			bb[bi++]=ByteBuffer.wrap(lastMod);
		}
		if(contentType!=null){
			bb[bi++]=ByteBuffer.wrap(h_content_type);
			bb[bi++]=ByteBuffer.wrap(contentType);
		}
		if(content!=null){
			bb[bi++]=ByteBuffer.wrap(h_content_length);
			bb[bi++]=ByteBuffer.wrap(Long.toString(content.length).getBytes());
		}
		if(connection_keep_alive)
			bb[bi++]=ByteBuffer.wrap(hkp_connection_keep_alive);
		bb[bi++]=ByteBuffer.wrap(ba_crlf2);
		if(content!=null)
			bb[bi++]=ByteBuffer.wrap(content);
		final long n=sendpacket(bb,bi);
		thdwatch.output+=n;
		state=state_nextreq;
	}
	private int sendpacket(final ByteBuffer[]bba,final int n)throws Throwable{
		if(b.print_reply_headers){
			for(int i=0;i<n;i++){
				final ByteBuffer bb=bba[i];
				b.p(new String(bb.array(),bb.position(),bb.limit()));
			}
		}
		long tosend=0;
		for(int i=0;i<n;i++)tosend+=bba[i].remaining();
		final long c=sockch.write(bba,0,n);//? while
		if(c!=tosend)b.log(new Error("sent "+c+" of "+tosend+" bytes"));//? throwerror
		return n;
	}
//	private void reply2(final chdresp c)throws Throwable{
//		final ByteBuffer bb=ByteBuffer.allocate(512+c.byteBuffer().limit());
//		//? bba
//		bb.put(h_http200);
//		bb.put(h_content_length).put(Long.toString(c.byteBuffer().limit()).getBytes());
//		if(c.lastModified()!=null)bb.put(h_last_modified).put(c.lastModified().getBytes());
//		if(c.contentType()!=null)bb.put(h_content_type).put(c.contentType().getBytes());
//		bb.put(hkp_connection_keep_alive);
//		if(sesid_set){
//			for(final ByteBuffer b:new ByteBuffer[]{ByteBuffer.wrap(hk_set_cookie),ByteBuffer.wrap(sesid.getBytes()),ByteBuffer.wrap(hkv_cookie_append)})bb.put(b);
//			sesid_set=false;
//		}
//		bb.put(req.ba_crlf2);
//		bb.flip();
//		transferbuffers(new ByteBuffer[]{bb,c.byteBuffer().slice()});
//	}
	private void transferbuffers(final ByteBuffer[]bba)throws Throwable{
		if(b.print_reply_headers){
			for(final ByteBuffer bb:bba){
				final byte[]ba;
				if(bb.hasArray()){
					ba=bb.array();
					final int header_len=find_end_of_header(ba);
					if(header_len==-1)
						p(new String(bb.array(),bb.position(),bb.remaining()));
					else{
						p(new String(bb.array(),bb.position(),header_len));
						break;
					}
				}else{
					ba=new byte[bb.remaining()];
					bb.get(ba);
					bb.position(bb.position()-ba.length);
					final int header_len=find_end_of_header(ba);
					if(header_len==-1)
						p(new String(ba));
					else{
						p(new String(ba,0,header_len));
						break;
					}
				}
			}
		}else if(b.print_replies){
			for(final ByteBuffer bb:bba)
				if(bb.hasArray())
					p(new String(bb.array(),bb.position(),bb.remaining()));
				else{
//					pl("pos:"+bb.position());
					final byte[]ba=new byte[bb.remaining()];
					bb.get(ba);
//					pl("posa:"+bb.position());
					bb.position(bb.position()-ba.length);
					p(new String(ba));
				}
		}
		
		long n=0;for(final ByteBuffer b:bba)n+=b.remaining();
		transfer_buffers=bba;
		transfer_buffers_remaining=n;
		state=state_transfer_buffers;do_transfer_buffers();
	}
	private int find_end_of_header(final byte[]ba){
		final byte[]match=ba_crlf2;
		final int match_len=match.length;
		int match_i=0;
		int i=0;
//		final String bastr=new String(ba);
		while(true){
			final byte b=ba[i];
			if(b==match[match_i]){
				match_i++;
				if(match_i==match_len)
					return i;
			}else{
				match_i=0;
			}
			i++;
			if(i==ba.length)
				return-1;
		}
	}
	boolean do_transfer()throws Throwable{
		if(state==state_transfer_file)return do_transfer_file();
		else if(state==state_transfer_buffers)return do_transfer_buffers();
		else throw new IllegalStateException();
	}
	private boolean do_transfer_buffers()throws Throwable{
		while(transfer_buffers_remaining!=0){
			final long c=sockch.write(transfer_buffers);
			if(c==0)return false;
			transfer_buffers_remaining-=c;
			thdwatch.output+=c;
		}
		state=state_nextreq;
		return true;
	}
	private boolean do_transfer_file()throws IOException{
		//? heavy
		//		final long buf_size=this.sockch.socket().getSendBufferSize();
		final int buf_size=b.transfer_file_write_size;
		while(transfer_file_remaining!=0)try{
			final long c=transfer_file_channel.transferTo(transfer_file_position,buf_size,sockch);
			if(c==0)return false;
			transfer_file_position+=c;
			transfer_file_remaining-=c;
			thdwatch.output+=c;
		}catch(final IOException e){
			//?
			//? differingmessagedependingonplatform
			//				if("sendfile failed: EAGAIN (Try again)".equals(e.getMessage())){
			//					thdwatch.eagain++;
			//					b.log(e);
			//					return false;
			//				}
			//				throw e;
			final String msg=e.getMessage();
			if(e instanceof IOException&&(
					"Broken pipe".equals(msg)||
					"Connection reset by peer".equals(msg)||
					"sendfile failed: EPIPE (Broken pipe)".equals(msg)||//? android (when closing browser while transfering file)
					"An existing connection was forcibly closed by the remote host".equals(msg)
					)
			){
				close();
				return false;
			}
			thdwatch.eagain++;//? assuming. eventually bug
			return false;
		}
		transfer_file_channel.close();
		state=state_nextreq;
		return true;
	}
	private void parse_content_read(){
		final int c=(int)(ba_rem>contentLength?contentLength:ba_rem);
		bb_content.put(ba,ba_pos,c);
		contentLength-=c;ba_pos+=c;ba_rem-=c;
		if(contentLength==0){
			bb_content.flip();
			state=state_waiting_run_page_content;
		}
	}
	private void parse_content_upload()throws Throwable{
//		System.out.println(path_s+"   content upload");
		final long diff=contentLength-ba_rem;
		final int c;
		if(diff<0){
			final int lim=bb.limit();
			bb.limit(ba_pos+(int)contentLength);
			c=upload_channel.write(bb);
			bb.limit(lim);
		}else{
			c=upload_channel.write(bb);
		}
		contentLength-=c;ba_pos+=c;ba_rem-=c;thdwatch.input+=c;
		if(contentLength<0)//?
			throw new Error();
		if(contentLength==0){
			upload_channel.close();
			final SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd--HH:mm:ss.SSS");
			try{upload_path.lastmod(df.parse(upload_lastmod_s).getTime());}
			catch(final ParseException e){throw new Error(e);}
			catch(final Throwable ignored){System.out.println(ignored);}//? forandroid
			reply(h_http204,null,null,null);
			state=state_nextreq;
		}
	}
	// threaded
	void run_page()throws Throwable{
		state=state_run_page;
		resp_page();
		if(state==state_sock){thdwatch.socks++;return;}
		if(state==state_run_page)state=state_nextreq;
	}
	static private session load_session(final String session_id)throws IOException{
		final File serfile=new File(b.root_dir+'/'+b.sessions_dir+'/'+session_id+'/'+b.sessionfile);
		pl("session load "+serfile);
		if(!serfile.exists())return null;
		try(final InputStream is=new FileInputStream(serfile)){
			try(final ObjectInputStream ois=new ObjectInputStream(is)){
				return (session)ois.readObject();
			}catch(final Throwable t){
				b.pl("while loading session "+session_id+": "+t);
				return null;
			}
		}
	}
	@SuppressWarnings("unchecked") private void resp_page()throws Throwable{
		if(sesid!=null&&ses==null){//?? sessiongetandloadracing 
			ses=session.all().get(sesid);
			if(ses==null&&b.sessionfile_load){
				ses=load_session(sesid);
				if(ses==null){
					pl("could not load, new session with same id "+sesid);
					ses=new session(sesid);					
				}
				ses.bits(b.get_session_bits_for_sessionid(sesid));
				session.all().put(sesid,ses);
				thdwatch.sessions++;
			}
		}else if(sesid==null){
			sesid=mkcookieid();
			sesid_set=true;
			pl("new session "+sesid);
			ses=new session(sesid);
			ses.bits(b.get_session_bits_for_sessionid(sesid));
			session.all().put(sesid,ses);
			thdwatch.sessions++;
		}
		ses.nreq++;
		a e=(a)ses.get(path_s);
		if(e==null){
			String cn=path_s.replace('/','.');
			while(cn.startsWith("."))cn=cn.substring(1);
			cn=b.webobjpkg+cn;
			Class<? extends a>ecls;
			try{ecls=(Class<? extends a>)Class.forName(cn);}catch(Throwable e1){try{
				final String clsnm=cn+(cn.length()==0||cn.endsWith(".")?"":".")+b.default_package_class;
				ecls=(Class<? extends a>)Class.forName(clsnm);
			}catch(Throwable e2){
				while(e1.getCause()!=null)e1=e1.getCause();
				final xwriter x=new xwriter().p(path_s).nl().nl().p(b.stacktraceline(e1)).nl().nl().p(b.stacktraceline(e2)).nl();
				pl(x.toString());
				reply(h_http404,null,null,tobytes(x.toString()));
				return;
			}}
			try{e=ecls.getConstructor().newInstance();}catch(Throwable ex){
				while(ex.getCause()!=null)ex=ex.getCause();
//				final xwriter x=new xwriter().p(path_s).nl().nl().p(b.stacktraceline(ex)).nl();
				reply(h_http404,null,null,tobytes(stacktrace(ex)));
				return;
			}
			if(e instanceof sock){
				state=state_sock;
				sck=(sock)e;
				switch(sck.sockinit(hdrs,new sockio(sockch,selkey,ByteBuffer.wrap(ba,ba_pos,ba_rem)))){default:throw new IllegalStateException();
				case read:selkey.interestOps(SelectionKey.OP_READ);selkey.selector().wakeup();break;
				case write:selkey.interestOps(SelectionKey.OP_WRITE);selkey.selector().wakeup();break;
				case close:sockch.close();break;
				case wait:selkey.interestOps(0);break;
				}
				return;
			}
			ses.put(path_s,e);
		}
		if(!content.isEmpty()){
			if(b.acl_on)b.acl_ensure_post(e);
			String ax="";
			for(final Map.Entry<String,String>me:content.entrySet()){
				if(axfld.equals(me.getKey())){
					ax=me.getValue();
					continue;
				}
				//? indexofloop
				final String[]pth=me.getKey().split(req.field_path_separator);
				a ee=e;
				for(int n=1;n<pth.length;n++){
					ee=ee.chld(pth[n]);
					if(ee==null)throw new Error("not found: "+me.getKey());
				}
				ee.set(me.getValue());
			}
			if(ax.length()==0)return;//? y
			final String axid,axfunc,axarg;
			final int i1=ax.indexOf(' ');
			if(i1==-1){
				axid=ax;
				axfunc=axarg="";
			}else{
				axid=ax.substring(0,i1);
				final int i2=ax.indexOf(' ',i1+1);
				if(i2==-1){
					axfunc=ax.substring(i1+1);
					axarg="";
				}else{
					axfunc=ax.substring(i1+1,i2);
					axarg=ax.substring(i2+1);
				}
			}
			final String[]pth=axid.split(req.field_path_separator);//? indexofloop
			for(int n=1;n<pth.length;n++){
				e=e.chld(pth[n]);
				if(e==null)break;
			}
			final oschunked os=reply_chunked(h_http200,text_html_utf8,null);
			final xwriter x=new xwriter(os);
			if(e==null){
				x.xalert("element not found:\n"+axid);
				os.finish();
				return;
			}
			try{e.getClass().getMethod("x_"+axfunc,xwriter.class,String.class).invoke(e,x,axarg);}
			catch(final InvocationTargetException t){
				b.log(t);
				x.xalert(b.isempty(t.getTargetException().getMessage(),t.toString()));
			}catch(NoSuchMethodException t){
				x.xalert("method not found:\n"+e.getClass().getName()+".x_"+axfunc+"(xwriter,String)");
			}
			os.finish();
			return;
		}
		if(b.cache_uris){
			if(e instanceof cacheable){
				final cacheable cw=(cacheable)e;
				reply(cw);
				thdwatch.cacheu++;
				return;
			}
		}
		final boolean isbin=e instanceof bin;
		final oschunked os=reply_chunked(h_http200,isbin?((bin)e).contenttype():text_html_utf8,null);
		final xwriter x=new xwriter(os);
		if(!isbin){
			os.write(ba_page_header_pre_title);
//			if(e instanceof a.titled){
//				((a.titled)e).title_to(x);
//			}else{
//				final String cn=e.getClass().getName();
//				x.p(cn.substring(cn.lastIndexOf('.')+1));
//			}
//			os.write(ba_page_header_after_title);
		}
		try{e.to(x);}catch(final Throwable t){b.log(t);x.pre().p(b.stacktrace(t));}
		os.finish();
	}
	// threaded done
	private oschunked reply_chunked(final byte[]hdr,final String contentType,final String lastmod)throws Throwable{
		final ByteBuffer[]bb_reply=new ByteBuffer[11];
		int bbi=0;
		bb_reply[bbi++]=ByteBuffer.wrap(hdr);
		if(sesid_set){
			bb_reply[bbi++]=ByteBuffer.wrap(hk_set_cookie);
			bb_reply[bbi++]=ByteBuffer.wrap(sesid.getBytes());
			bb_reply[bbi++]=ByteBuffer.wrap(hkv_cookie_append);
			sesid_set=false;
		}
		if(connection_keep_alive)
			bb_reply[bbi++]=ByteBuffer.wrap(hkp_connection_keep_alive);
		if(contentType!=null){
			bb_reply[bbi++]=ByteBuffer.wrap(h_content_type);
			bb_reply[bbi++]=ByteBuffer.wrap(contentType.getBytes());
		}
		if(lastmod!=null){
			bb_reply[bbi++]=ByteBuffer.wrap(h_last_modified);
			bb_reply[bbi++]=ByteBuffer.wrap(lastmod.getBytes());
		}
		bb_reply[bbi++]=ByteBuffer.wrap(hkp_transfer_encoding_chunked);
		bb_reply[bbi++]=ByteBuffer.wrap(ba_crlf2);
		thdwatch.output+=sendpacket(bb_reply,bbi);//? sends2packs
		return new oschunked(this,b.chunk_B);     //?
	}
	private void reply(final cacheable cw)throws Throwable{
		final String ifmodsince=hdrs.get(hk_if_modified_since);
		final String lastmod=cw.lastmod();
		if(ifmodsince!=null&&ifmodsince.equals(lastmod)){reply(h_http304,null,null,null);return;}
		final long t=System.currentTimeMillis();
		String key=sb_path.toString();
		if(cw.cacheforeachuser())key=req.get().session().id()+"~"+key;
		chdresp c=cacheu.get(key);
		if(c==null){c=new chdresp(cw,key);cacheu.put(key,c);}
		c.validate(t,ifmodsince);
		reply(c);
	}
	void run_page_content()throws Throwable{
		state=state_run_page_content;
		if(!contentType.startsWith(text_plain))throw new Error("only "+text_plain+" post allowed");
		parse_content();
		resp_page();
		if(state==state_run_page_content)state=state_nextreq;
		thdwatch.posts++;
	}
	private void parse_content()throws IOException{//? ycopyfrombb
		final byte[]ba=bb_content.array();
		int i=0;
		String name="";
		int s=0;
		int k=0;for(final byte c:ba){
			switch(s){default:throw new Error();
			case 0:if(c=='='){name=new String(ba,i,(k-i),b.strenc);i=k+1;s=1;}break;
			case 1:if(c=='\r'){final String value=new String(ba,i,(k-i),b.strenc);content.put(name,value);i=k+1;s=0;}break;
		}
		k++;}
		bb_content=null;
	}
	// socks
	boolean is_sock(){return state==state_sock;}
	sock.op sockread()throws Throwable{return sck.read();}
	sock.op sockwrite()throws Throwable{return sck.write();}
	// threaded socks
	boolean is_sock_thread(){return sck instanceof threadedsock;}
	private boolean waiting_sock_thread_read;
	private boolean waiting_sock_thread_write;
	void set_waiting_sock_thread_read(){waiting_sock_thread_read=true;}
	void set_waiting_sock_thread_write(){waiting_sock_thread_write=true;}
	void sock_thread_run()throws Throwable{
		if(waiting_sock_thread_read){waiting_sock_thread_read=false;sock_thread_read();}
		if(waiting_sock_thread_write){waiting_sock_thread_write=false;sock_thread_write();}
	}
	private void sock_thread_read()throws Throwable{
		switch(sockread()){default:throw new Error();
		case read:selkey.interestOps(SelectionKey.OP_READ);selkey.selector().wakeup();return;
		case write:selkey.interestOps(SelectionKey.OP_WRITE);selkey.selector().wakeup();return;
		case close:close();thdwatch.socks--;return;
		case wait:selkey.interestOps(0);return;
		case noop:return;
		}
	}
	private void sock_thread_write()throws Throwable{
		switch(sockwrite()){default:throw new Error();
		case read:selkey.interestOps(SelectionKey.OP_READ);selkey.selector().wakeup();break;
		case write:selkey.interestOps(SelectionKey.OP_WRITE);selkey.selector().wakeup();break;
		case close:close();thdwatch.socks--;break;
		}
	}

	boolean is_waiting_write(){return waiting_write;}
	void waiting_write(final boolean b){waiting_write=b;}
	boolean is_connection_keepalive(){return connection_keep_alive;}
	boolean is_transfer(){return state==state_transfer_file||state==state_transfer_buffers;}
	boolean is_waiting_run_page(){return state==state_waiting_run_page;}
	boolean is_waiting_run_page_content(){return state==state_waiting_run_page_content;}
	boolean is_waiting_run(){return is_waiting_run_page()||is_waiting_run_page_content();}
	void close(){
		try{if(is_sock())sck.onconnectionlost();}catch(final Throwable t){b.log(t);}
		try{sockch.close();}catch(final Throwable t){b.log(t);}
	}
	boolean is_buf_empty(){return ba_rem ==0;}

	public InetAddress ip(){return sockch.socket().getInetAddress();}
	public String host(){final String h=hdrs.get("host");final String[]ha=h.split(":");return ha[0];}
	public int port(){final String h=hdrs.get("host");final String[]ha=h.split(":");if(ha.length<2)return 80;return Integer.parseInt(ha[1]);}
	public String path(){return path_s;}
	public String query(){return query_s;}
	public session session(){return ses;}
	public String toString(){return new String(ba,ba_pos,ba_rem)+(bb_content==null?"":new String(bb_content.slice().array()));}
	public Map<String,String>headers(){return hdrs;}
	
	
	public static req get(){return((thdreq)Thread.currentThread()).r;}
	public static long cachef_size(){
		if(cachef==null)return 0;
		long k=0;
		//? sync(cachef)
		for(final chdresp e:cachef.values())k+=e.byteBuffer().capacity();
		return k;
	}
	public static long cacheu_size(){
		if(cacheu==null)return 0;
		//? sync(cacheu)
		long k=0;
		for(final chdresp e:cacheu.values()){
			if(e.byteBuffer()==null)continue;
			k+=e.byteBuffer().capacity();
		}
		return k;
	}

	private int state=state_method;
	private final ByteBuffer bb=ByteBuffer.allocate(b.reqinbuf_B);
	private ByteBuffer bb_content;
	private byte[]ba;
	private int ba_rem;
	private int ba_pos;
	private boolean connection_keep_alive;
	private final StringBuilder sb_path=new StringBuilder(128);
//	private final ByteBuffer bb_path=ByteBuffer.allocate(128);
	private String path_s;
	private String query_s;
	private path pth;
	private final StringBuilder sb_header_name=new StringBuilder(32);
	private final StringBuilder sb_header_value=new StringBuilder(128);
//	private final ByteBuffer bb_header_name=ByteBuffer.allocate(32);
//	private final ByteBuffer bb_header_value=ByteBuffer.allocate(128);
	private final Map<String,String>hdrs=new HashMap<String,String>();
	private String sesid;
	private session ses;
	private boolean sesid_set;
	private String contentType;
	private long contentLength;
	private final Map<String,String>content=new HashMap<String,String>();
	private ByteBuffer[]transfer_buffers;
	private long transfer_buffers_remaining;
	private FileChannel transfer_file_channel;
	private long transfer_file_position;
	private long transfer_file_remaining;
	private boolean waiting_write;
	SelectionKey selkey;
	SocketChannel sockch;
	private path upload_path;
	private FileChannel upload_channel;
	private String upload_lastmod_s;
	private sock sck;
	final public static String field_path_separator="-";

	private static Map<String,chdresp>cachef;
	private static Map<String,chdresp>cacheu;
	static void init_static(){
		if(b.cache_files)cachef=Collections.synchronizedMap(new LinkedHashMap<String,chdresp>(b.cache_files_hashlen));
		if(b.cache_uris)cacheu=Collections.synchronizedMap(new LinkedHashMap<String,chdresp>());
	}

	final static byte[]h_http200="HTTP/1.1 200 OK".getBytes();
	final static byte[]h_content_length="\r\nContent-Length: ".getBytes();
	final static byte[]h_last_modified="\r\nLast-Modified: ".getBytes();
	final static byte[]h_content_type="\r\nContent-Type: ".getBytes();
	final static byte[]hkp_connection_keep_alive="\r\nConnection: Keep-Alive".getBytes();
	final static byte[]ba_crlf2="\r\n\r\n".getBytes();
	private final static String axfld="$";
	private final static byte[]h_http204="HTTP/1.1 204 No Content".getBytes();
	private final static byte[]h_http206="HTTP/1.1 206 Partial Content".getBytes();
	private final static byte[]h_http304="HTTP/1.1 304 Not Modified".getBytes();
	final static byte[]h_http403="HTTP/1.1 403 Forbidden".getBytes();
	private final static byte[]h_http404="HTTP/1.1 404 Not Found".getBytes();
	private final static byte[]hk_set_cookie ="\r\nSet-Cookie: i=".getBytes();
	private final static byte[]hkp_transfer_encoding_chunked="\r\nTransfer-Encoding: chunked".getBytes();
	private final static byte[]hkp_accept_ranges_byte="\r\nAccept-Ranges: bytes".getBytes();
	private final static byte[]hk_content_range ="\r\nContent-Range: ".getBytes();
	private final static byte[]hkv_cookie_append =";path=/;expires=Thu, 31-Dec-2099 00:00:00 GMT;".getBytes();
	private final static String hk_connection="connection";
	private final static String hk_content_length="content-length";
	private final static String hk_content_type="content-type";
	private final static String hk_cookie="cookie";
	private final static String hk_if_modified_since="if-modified-since";
	private final static String hv_keep_alive="keep-alive";
	private final static String s_bytes_="bytes ";
	private final static String s_eq="=";
	private final static String s_minus="-";
	private final static String s_range="range";
	private final static String s_slash="/";
	private final static byte[]ba_page_header_pre_title="<!doctype html><meta charset=utf-8><link rel=stylesheet href=/x.css><script src=/x.js></script>".getBytes();
	private final static int state_nextreq=0;
	private final static int state_method=1;
	private final static int state_uri=2;
	private final static int state_prot=3;
	private final static int state_header_name=4;
	private final static int state_header_value=5;
	private final static int state_content_read=6;
	private final static int state_transfer_file=7;
	private final static int state_transfer_buffers=8;
	private final static int state_waiting_run_page=9;
	private final static int state_waiting_run_page_content=10;
	private final static int state_run_page=11;
	private final static int state_run_page_content=12;
	private final static int state_content_upload=13;
	private final static int state_sock=15;
	private final static String text_html_utf8="text/html;charset=utf-8";
	private final static String text_plain="text/plain";
//	private final static String text_plain_utf8="text/plain;charset=utf-8";
}
