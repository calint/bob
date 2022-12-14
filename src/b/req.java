package b;

import static b.b.tobytes;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.List;
import java.util.Map;
import b.a.stateless;
import b.b.conf;
import db.Db;
import db.DbObject;
import db.DbTransaction;
import db.Query;

public final class req{
	void process() throws Throwable{
		while(true){
			// check if there are ongoing writes
			if(is_transfer()){
				do_transfer();
				if(is_transfer())
					return;
				if(!is_connection_keepalive()){
					close();
					return;
				}
			}
			if(is_oschunked_waiting_write()){// ? is..waiting not synchronized on request ok? spurious notify...
				synchronized(this){
					notify();
				}
				return;
			}
			// check if buffer is fully processed and make a new read
			if(ba_rem==0){
				bb.clear();
				final int n=socket_channel.read(bb);
				bb.flip();
				ba=bb.array();
				ba_pos=bb.position();
				ba_rem=bb.remaining();
				if(n==0){
					selection_key.interestOps(SelectionKey.OP_READ);
					return;
				}
				if(n==-1){
					close();
					return;
				}
//				System.out.println(toString());
				thdwatch.input+=n;
			}
			while(ba_rem>0){
				switch(st){
				default:
					throw new RuntimeException();
				case next_request:
					reset();
					st=state.method;
					// fallthrough
				case method:
					parse_method();
					break;
				case uri:
					parse_uri();
					break;
				case prot:
					parse_prot();
					break;
				case header_name:
					parse_header_name();
					// parse_header_name()->do_after_header() might have changed the state and started transfer
					if(is_transfer())
						return;
					if(is_waiting_run_page()){
//						System.out.println("interest ops: "+selection_key.interestOps());
						b.thread(this);
//						break; // more requests might be in buffer but if more threads are spawned using the same socket channel then writes would not be in sequence
						return; // ! will hang client on chained requests
					}
					break;
				case header_value:
					parse_header_value();
					break;
				case content_read:
					parse_content_read();
					// state might have changed
					if(is_waiting_run_page()){
						b.thread(this);
//						break; // more requests might be in buffer but if more threads are spawned using the same socket channel then writes might not be in sequence
						return; // ! will hang client on chained requests
					}
					break;
				case content_upload:
					parse_content_upload();
					break;
				}
			}
			if(st==state.next_request&&!is_connection_keepalive()){
				close();
				return;
			}
		}
	}

	private void reset(){
		method_length=0;
		uri_sb.setLength(0);
		uri_length=0;
		path_str=null;
		path=null;
		query_str=null;
		prot_length=0;
		header_name_length=0;
		header_name_sb.setLength(0);
		header_value_length=0;
		header_value_sb.setLength(0);
		headers_count=0;
		headers.clear();
		session_id=null;
		session_id_set=false;
		content_bb=null;
		content_type=null;
		content_remaining_to_read=0;
		content.clear();
		transfer_buffers=null;
		transfer_buffers_remaining=0;
		transfer_file_channel=null;
		transfer_file_position=0;
		transfer_file_remaining=0;
		oschunked_waiting_write=false;
		upload_path=null;
		upload_channel=null;
		upload_lastmod_s=null;
		websock=null;
	}

	private void parse_method(){
		final int ba_pos_prev=ba_pos;
		while(ba_rem!=0){
			final byte b=ba[ba_pos++];
			ba_rem--;
			if(b==' '){
				st=state.uri;
				break;
			}
		}
		method_length+=ba_pos-ba_pos_prev;
		if(method_length>abuse_method_len){
			close();
			throw new RuntimeException("abusemethodlen "+method_length+": "+new String(ba,ba_pos_prev,ba_pos-ba_pos_prev));
		}
	}

	private void parse_uri(){
		final int ba_pos_prev=ba_pos;
		while(ba_rem!=0){
			final byte b=ba[ba_pos++];
			ba_rem--;
			if(b==' '){
				st=state.prot;
				break;
			}
			if(b=='\n'){
				do_after_prot();
				break;
			} // ie: '. index.html\n'
			uri_sb.append((char)b);
		}
		uri_length+=ba_pos-ba_pos_prev;
		if(uri_length>abuse_uri_len){
			close();
			throw new RuntimeException("abuseurilen "+uri_length+": "+uri_sb);
		}
	}

	private void parse_prot() throws Throwable{
		final int ba_pos_prev=ba_pos;
		while(ba_rem!=0){
			final byte b=ba[ba_pos++];
			ba_rem--;
			if(b!='\n'){
				continue;
			}
			if(ba_pos>=3&&ba[ba_pos-3]=='1'||ba_pos>=2&&ba[ba_pos-2]=='1'){
				connection_keep_alive=true;// ? cheapo to set keepalive for http/1.1\r\n
			}
			do_after_prot();
			break;
		}
		prot_length+=ba_pos-ba_pos_prev;
		if(prot_length>abuse_prot_len){
			close();
			throw new RuntimeException("abuseprotlen "+prot_length);
		}
	}

	private void do_after_prot(){
		thdwatch.reqs++;
		final String uri_encoded=uri_sb.toString().trim();
		final int i=uri_encoded.indexOf('?');
		if(i==-1){
			path_str=b.urldecode(uri_encoded);
			query_str="";
		}else{
			path_str=b.urldecode(uri_encoded.substring(0,i));
			query_str=uri_encoded.substring(i+1);
		}
		st=state.header_name;
	}

	private void parse_header_name() throws Throwable{
		final int ba_pos_prev=ba_pos;
		while(ba_rem!=0){
			final byte b=ba[ba_pos++];
			ba_rem--;
			if(b==':'){
				header_value_sb.setLength(0);
				header_value_length=0;
				st=state.header_value;
				break;
			}
			if(b=='\n'){
				do_after_header();
				return;
			}
			header_name_sb.append((char)b);
		}
		header_name_length+=ba_pos-ba_pos_prev;
		if(header_name_length>abuse_header_name_len){
			close();
			throw new RuntimeException("abuseheadernamelen "+header_name_length+": "+header_name_sb);
		}
	}

	private void parse_header_value(){
		final int ba_pos_prev=ba_pos;
		while(ba_rem!=0){
			final byte b=ba[ba_pos++];
			ba_rem--;
			if(b=='\n'){
				headers.put(header_name_sb.toString().trim().toLowerCase(),header_value_sb.toString().trim());
				headers_count++;
				if(headers_count>abuse_header_count){
					close();
					throw new RuntimeException("abuseheaderscount "+headers_count);
				}
				header_name_sb.setLength(0);
				header_name_length=0;
				st=state.header_name;
				break;
			}
			header_value_sb.append((char)b);
		}
		header_value_length+=ba_pos-ba_pos_prev;
		if(header_value_length>abuse_header_value_len){
			close();
			throw new RuntimeException("abuseheadervaluelen "+header_value_length+": "+header_value_sb);
		}
	}

	private void do_after_header() throws Throwable{
//		// this would trigger set-cookie on files and resources
//		if(!set_session_id_from_cookie()){
//			session_id=make_new_session_id();
//			session_id_set=true;
//			pl("new session "+session_id);
//			thdwatch.sessions++;
//		}

		final String ka=headers.get(hk_connection);
		if(ka!=null){
			connection_keep_alive=hv_keep_alive.equalsIgnoreCase(ka);
		}

		content_type=headers.get(hk_content_type);
		if(content_type!=null){
			if(content_type.startsWith("dir;")||"dir".equals(content_type)){
				if(!b.enable_upload)
					throw new RuntimeException("uploadsdisabled");
				if(!set_session_id_from_cookie())
					throw new RuntimeException("nocookie at create dir. path:"+uri_sb);
				final String[] q=content_type.split(";");
				final String lastmod_s=q[1];
				final path p=b.path(b.sessions_dir).get(session_id).get(path_str);
				if(!p.exists()){
					p.mkdirs();
				}
				if(!p.isdir())
					throw new RuntimeException("isnotdir: "+p);
				final SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd--HH:mm:ss.SSS");
				try{
					p.lastmod(df.parse(lastmod_s).getTime());
				}catch(final ParseException e){
					throw new RuntimeException(e);
				}
				reply(h_http204,null,null,null);
				return;
			}
			if(content_type.startsWith("file;")||"file".equals(content_type)){
				if(!b.enable_upload)
					throw new RuntimeException("uploadsdisabled");
//				System.out.println(path_s);
				if(!set_session_id_from_cookie())
					throw new RuntimeException("nocookie at create file from upload. path:"+uri_sb);
//				final String contentLength_s=hdrs.get(hk_content_length);
				content_remaining_to_read=Long.parseLong(headers.get(hk_content_length));
				if(content_remaining_to_read>abuse_upload_len){
					close();
					throw new RuntimeException("abuseuploadlen "+content_remaining_to_read);
				}
				final String[] q=content_type.split(";");
				upload_lastmod_s=q.length>1?q[1]:new SimpleDateFormat("yyyy-MM-dd--HH:mm:ss.SSS").format(new Date());
				// final String time=q[1];
				// final String size=q[2];
				// final String md5=q[3];
				// final String range=q[4];
				try{
					upload_path=b.path(b.sessions_dir).get(session_id).get(path_str);
				}catch(final Throwable t){
					close();
					throw t;
				}
				upload_channel=upload_path.filechannel();
				bb.position(ba_pos);
				st=state.content_upload;
				parse_content_upload();
				return;
			}
		}
		// assumes content type "text/plain; charset=utf-8" from an ajax post
		final String contentLength_s=headers.get(hk_content_length);
		if(contentLength_s!=null&&!"0".equals(contentLength_s)){
			if(!set_session_id_from_cookie())
				throw new RuntimeException("nocookie in request with content. path:"+uri_sb);
			content_remaining_to_read=Long.parseLong(contentLength_s);
			if(content_remaining_to_read>abuse_content_len){
				close();
				throw new RuntimeException("abusecontentlen "+content_remaining_to_read);
			}
			content_bb=ByteBuffer.allocate((int)content_remaining_to_read);
			st=state.content_read;
			parse_content_read();
			return;
		}
		try{
			path=b.path(path_str);
		}catch(final Throwable t){
			reply(h_http404,null,null,tobytes(b.stacktrace(t)));
			close();
			throw t;
		}
		if(b.cache_files&&try_cache()||b.try_file&&try_file())
			return;
		if(b.try_rc&&try_resource())
			return;

		// try creating an instance of 'a' on a separate thread
		st=state.waiting_run_page;
	}

	public static String get_session_id_from_headers(final Map<String,String> headers){
		final String cookie=headers.get(req.hk_cookie);
		if(cookie==null)
			return null;
		final String[] c1=cookie.split(";");
		for(String cc:c1){
			cc=cc.trim();
			if(cc.startsWith("i="))
				return cc.substring("i=".length());
		}
		return null;
	}

	private boolean set_session_id_from_cookie(){
		session_id=get_session_id_from_headers(headers);
		return session_id!=null;
	}

	private static String make_new_session_id(){
		final SimpleDateFormat sdf=new SimpleDateFormat("yyMMdd-HHmmss.SSS-");
		final StringBuilder sb=new StringBuilder(sdf.format(new Date()));
		final String alf="0123456789abcdef";
		for(int n=0;n<8;n++){
			sb.append(alf.charAt(b.rndint(0,alf.length())));
		}
		return sb.toString();
	}

	private boolean try_file() throws Throwable{
		if(!path.exists())
			return false;
		if(path.isdir()){
			final path p=path.get(b.default_directory_file);
			if(!p.exists()||!p.isfile())
				return false;
			path=p;
		}
		thdwatch.files++;
		if(path.size()<=b.cache_files_maxsize){
			final String suffix=path_str.substring(path_str.lastIndexOf('.')+1);
			final byte[] content_type=b.get_content_type_for_file_suffix(suffix);
			final chdresp cr=new chdresp_file(path,content_type);
			file_and_resource_cache.put(path_str,cr);
			reply(cr);
			thdwatch._cachef++;
			return true;
		}

		final long lastmod_l=path.lastmod();
		final String etag="\""+lastmod_l+"\"";
		final String client_etag=headers.get(hk_if_none_match);
		if(etag.equals(client_etag)){
			reply(h_http304,null,null,null);
			return true;
		}

		final long len=path.size();

		final String range_s=headers.get(s_range);
		long range_from;
		long range_to;
		final ByteBuffer[] bb=new ByteBuffer[16];
		int i=0;
		final String suffix=path_str.substring(path_str.lastIndexOf('.')+1);
		final byte[] content_type=b.get_content_type_for_file_suffix(suffix);
		if(range_s!=null){
			final String[] s=range_s.split(s_equals);
			final String[] ss=s[1].split(s_minus);

			try{
				range_from=Long.parseLong(ss[0]);
			}catch(final NumberFormatException e){
				range_from=-1;
			}
			if(range_from==-1){ // invalid or not specified
				range_from=0;
			}

			if(ss.length>1){
				try{
					range_to=Long.parseLong(ss[1]);
				}catch(final NumberFormatException e){
					range_to=-1;
				}
			}else{
				range_to=-1;
			}

			bb[i++]=ByteBuffer.wrap(h_http206);
			bb[i++]=ByteBuffer.wrap(h_content_length);

			if(range_to==-1){ // invalid or not specified
				bb[i++]=ByteBuffer.wrap(Long.toString(len-range_from).getBytes());
				bb[i++]=ByteBuffer.wrap(hk_content_range_bytes);
				bb[i++]=ByteBuffer.wrap((range_from+s_minus+(len-1)+s_slash+len).getBytes());// zero index and inclusive adjustment
			}else{ // range_to specified
				bb[i++]=ByteBuffer.wrap(Long.toString(range_to-range_from+1).getBytes());// zero index inclusive adjustment
				bb[i++]=ByteBuffer.wrap(hk_content_range_bytes);
				bb[i++]=ByteBuffer.wrap((range_from+s_minus+range_to+s_slash+len).getBytes());
			}
		}else{
			range_from=0;
			range_to=-1;
			bb[i++]=ByteBuffer.wrap(h_http200);
			bb[i++]=ByteBuffer.wrap(h_content_length);
			bb[i++]=ByteBuffer.wrap(Long.toString(len).getBytes());
		}
		if(content_type!=null){
			bb[i++]=ByteBuffer.wrap(h_content_type);
			bb[i++]=ByteBuffer.wrap(content_type);
		}
		bb[i++]=ByteBuffer.wrap(h_etag);
		bb[i++]=ByteBuffer.wrap(etag.getBytes());
		bb[i++]=ByteBuffer.wrap(hkp_accept_ranges_byte);
		if(session_id_set){
			bb[i++]=ByteBuffer.wrap(hk_set_cookie);
			bb[i++]=ByteBuffer.wrap(session_id.getBytes());
			bb[i++]=ByteBuffer.wrap(hkv_set_cookie_append);
//			session_id_set=false;
		}
		if(connection_keep_alive){
			bb[i++]=ByteBuffer.wrap(hkp_connection_keep_alive);
		}
		bb[i++]=ByteBuffer.wrap(ba_crlf2);
		final long n=send_packet(bb,i); // ? is send complete?
		thdwatch.output+=n;
		transfer_file_channel=path.fileinputstream().getChannel();
		transfer_file_position=range_from;
		if(range_to==-1){ // unspecified, use content_length
			transfer_file_remaining=len-range_from;
		}else{ // unspecified, use content_length
			transfer_file_remaining=range_to-range_from+1; // zero indexed inclusive adjustment
		}

		st=state.transfer_file;
		do_transfer_file(); // ? return value ignored
		return true;
	}

	/** @return true if the file or resource has been cached. */
	private boolean try_cache() throws Throwable{
		final chdresp cachedresp=file_and_resource_cache.get(path_str);
		if(cachedresp==null)
			return false;
		// validate that the cached response is up to date
		if(!cachedresp.validate(System.currentTimeMillis())){
			file_and_resource_cache.remove(path_str);
			thdwatch._cachef--;
			return false;
		}
		reply(cachedresp); // send the cached response
		return true;
	}

	/** @return true if resource was cached and sent. */
	private boolean try_resource() throws Throwable{
		final String resource_path=b.get_resource_for_path(path_str);
		if(resource_path==null)
			return false;

		final InputStream is=req.class.getResourceAsStream(resource_path);
		if(is==null)
			return false;
		final String suffix=path_str.substring(path_str.lastIndexOf('.')+1);
		final byte[] content_type=b.get_content_type_for_file_suffix(suffix);
		final chdresp c=new chdresp_resource(is,content_type);
		file_and_resource_cache.put(path_str,c);
		reply(c);
		return true;
	}

	private void reply(final chdresp c) throws Throwable{
		thdwatch.cachef++;
		final String clientetag=headers.get(hk_if_none_match);
		if(clientetag!=null&&c.etag_matches(clientetag)){
			reply(h_http304,null,null,null);
			return;
		}
		final String range_s=headers.get("range");
		if(range_s!=null){
			final long len=c.content_length_in_bytes();
			long range_from;
			long range_to;
			final String[] s=range_s.split(s_equals);
			final String[] ss=s[1].split(s_minus);

			try{
				range_from=Long.parseLong(ss[0]);
			}catch(final NumberFormatException e){
				range_from=-1;
			}
			if(range_from==-1){ // invalid or not specified
				range_from=0;
			}

			if(ss.length>1){
				try{
					range_to=Long.parseLong(ss[1]);
				}catch(final NumberFormatException e){
					range_to=-1;
				}
			}else{
				range_to=-1;
			}

			int bb_len=session_id_set?10:7;
			final byte[] content_type=c.content_type();
			if(content_type!=null){
				bb_len+=2;
			}
			final ByteBuffer[] bb=new ByteBuffer[bb_len];
			int i=0;
			bb[i++]=ByteBuffer.wrap(h_http206);
			bb[i++]=ByteBuffer.wrap(h_content_length);
			if(range_to==-1){ // invalid or not specified
				bb[i++]=ByteBuffer.wrap(Long.toString(len-range_from).getBytes());
				bb[i++]=ByteBuffer.wrap(hk_content_range_bytes);
				bb[i++]=ByteBuffer.wrap((range_from+s_minus+(len-1)+s_slash+len).getBytes());// zero index and inclusive adjustment
			}else{ // range_to specified
				bb[i++]=ByteBuffer.wrap(Long.toString(range_to-range_from+1).getBytes());// zero index inclusive adjustment
				bb[i++]=ByteBuffer.wrap(hk_content_range_bytes);
				bb[i++]=ByteBuffer.wrap((range_from+s_minus+range_to+s_slash+len).getBytes());
			}
			if(session_id_set){
				bb[i++]=ByteBuffer.wrap(hk_set_cookie);
				bb[i++]=ByteBuffer.wrap(session_id.getBytes());
				bb[i++]=ByteBuffer.wrap(hkv_set_cookie_append);
//				session_id_set=false;// cookie will be set
			}
			if(content_type!=null){ // ? is it necessary in ranged requests?
				bb[i++]=ByteBuffer.wrap(h_content_type);
				bb[i++]=ByteBuffer.wrap(content_type);
			}
			bb[i++]=ByteBuffer.wrap(ba_crlf2);
			final long from_position=c.content_position();
			if(range_to==-1){
				bb[i++]=(ByteBuffer)c.byte_buffer().slice().position((int)(from_position+range_from)).limit((int)(from_position+len));
			}else{
				bb[i++]=(ByteBuffer)c.byte_buffer().slice().position((int)(from_position+range_from)).limit((int)(from_position+range_to+1)); // 0 indexed and inclusive
			}
			transfer_buffers(bb);
			return;
		}
		if(!session_id_set){// no cookie to set
			transfer_buffers(new ByteBuffer[]{c.byte_buffer().slice()});
			return;
		}
		// cookie to set
		final ByteBuffer[] bba={c.byte_buffer().slice(),ByteBuffer.wrap(hk_set_cookie),ByteBuffer.wrap(session_id.getBytes()),ByteBuffer.wrap(hkv_set_cookie_append),c.byte_buffer().slice()};
		bba[0].limit(c.additional_headers_insertion_position()); // limit the first slice to the location where
																	// additional headers can be inserted
		bba[4].position(c.additional_headers_insertion_position()); // position the buffer (same as bb[0]) to the start
																	// of the remaining data, which is
																	// additional_headers_insertion_position
		transfer_buffers(bba);
//		session_id_set=false;
	}

	private void reply(final byte[] firstline,final byte[] lastMod,final byte[] content_type,final byte[] content) throws Throwable{
		final ByteBuffer[] bb=new ByteBuffer[16];
		int bi=0;
		bb[bi++]=ByteBuffer.wrap(firstline);
		if(session_id_set){
			bb[bi++]=ByteBuffer.wrap(hk_set_cookie);
			bb[bi++]=ByteBuffer.wrap(session_id.getBytes());
			bb[bi++]=ByteBuffer.wrap(hkv_set_cookie_append);
//			session_id_set=false;
		}
		if(lastMod!=null){
			bb[bi++]=ByteBuffer.wrap(h_last_modified);
			bb[bi++]=ByteBuffer.wrap(lastMod);
		}
		if(content_type!=null){
			bb[bi++]=ByteBuffer.wrap(h_content_type);
			bb[bi++]=ByteBuffer.wrap(content_type);
		}
		if(content!=null){
			bb[bi++]=ByteBuffer.wrap(h_content_length);
			bb[bi++]=ByteBuffer.wrap(Long.toString(content.length).getBytes());
		}
		if(connection_keep_alive){
			bb[bi++]=ByteBuffer.wrap(hkp_connection_keep_alive);
		}
		bb[bi++]=ByteBuffer.wrap(ba_crlf2);
		if(content!=null){
			bb[bi++]=ByteBuffer.wrap(content);
		}
		final long n=send_packet(bb,bi);
		thdwatch.output+=n;
		st=state.next_request;
	}

	private int send_packet(final ByteBuffer[] bba,final int n) throws Throwable{
		long tosend=0;
		for(int i=0;i<n;i++){
			tosend+=bba[i].remaining();
		}
		final long c=socket_channel.write(bba,0,n);// ? while
		if(c!=tosend){
			b.log(new RuntimeException("sent "+c+" of "+tosend+" bytes"));// ? throwerror
		}
		return n;
	}

	private void transfer_buffers(final ByteBuffer[] bba) throws Throwable{
		long n=0;
		for(final ByteBuffer b:bba){
			n+=b.remaining();
		}
		transfer_buffers=bba;
		transfer_buffers_remaining=n;
		st=state.transfer_buffers;
		do_transfer_buffers();
	}

	/** @return true if transfer is done, false if more writes are needed. */
	void do_transfer() throws Throwable{
		if(st==state.transfer_file){
			do_transfer_file();
		}else if(st==state.transfer_buffers){
			do_transfer_buffers();
		}else
			throw new IllegalStateException();
	}

	/** @return true if if transfer is done, more writes are needed. */
	private void do_transfer_buffers() throws Throwable{
		while(transfer_buffers_remaining!=0){
			final long c=socket_channel.write(transfer_buffers);
			if(c==0)
				return;
			transfer_buffers_remaining-=c;
			thdwatch.output+=c;
		}
		st=state.next_request;
	}

	/** @return true if if transfer is done, more writes are needed. */
	private void do_transfer_file() throws IOException{
		final int buf_size=b.transfer_file_write_size;
		while(transfer_file_remaining!=0){
			try{
				final long n=transfer_file_remaining>buf_size?buf_size:transfer_file_remaining;
				final long c=transfer_file_channel.transferTo(transfer_file_position,n,socket_channel);
				if(c==0){
					selection_key.interestOps(SelectionKey.OP_WRITE);
					return;
				}
				transfer_file_position+=c;
				transfer_file_remaining-=c;
				thdwatch.output+=c;
			}catch(final IOException e){
				final String msg=e.getMessage();
				if(e instanceof IOException&&(msg.startsWith("Broken pipe")||msg.startsWith("Connection reset by peer")||msg.startsWith("sendfile failed")|| // ? android (when closing browser while transfering file)
						msg.startsWith("An existing connection was forcibly closed by the remote host"))){
					close();
					return;
				}
				thdwatch.eagain++; // ? assuming. eventually bug
				return;
			}
		}
		transfer_file_channel.close();
		st=state.next_request;
	}

	private void parse_content_read(){
		// content might span over several buffer reads
		final int c=(int)(ba_rem>content_remaining_to_read?content_remaining_to_read:ba_rem);
		content_bb.put(ba,ba_pos,c);
		content_remaining_to_read-=c;
		ba_pos+=c;
		ba_rem-=c;
		if(content_remaining_to_read==0){
			content_bb.flip();
			st=state.waiting_run_page;
		}
	}

	private void parse_content_upload() throws Throwable{
		final long diff=content_remaining_to_read-ba_rem;
		final int c;
		if(diff<0){
			final int lim=bb.limit();
			bb.limit(ba_pos+(int)content_remaining_to_read);
			c=upload_channel.write(bb);
			bb.limit(lim);
		}else{
			c=upload_channel.write(bb);
		}
		content_remaining_to_read-=c;
		ba_pos+=c;
		ba_rem-=c;
		thdwatch.input+=c;
		if(content_remaining_to_read<0)
			throw new RuntimeException();
		if(content_remaining_to_read==0){
			upload_channel.close();
			final SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd--HH:mm:ss.SSS");
			try{
				upload_path.lastmod(df.parse(upload_lastmod_s).getTime());
			}catch(final ParseException e){
				throw new RuntimeException(e);
			}catch(final Throwable ignored){
				b.log(ignored);
			} // ? forandroid
			reply(h_http204,null,null,null);
		}
	}

	private session get_session(){
		final DbTransaction tn=Db.currentTransaction();
		final List<DbObject> lsses=tn.get(session.class,new Query(session.sessionId,Query.EQ,session_id),null,null);
		final session dbses;
		if(lsses.isEmpty()){
			dbses=(session)tn.create(session.class);
			dbses.session_id(session_id);
		}else{
			if(lsses.size()>1){
				b.log(new RuntimeException("found more than one dbsession for id "+session_id));
			}
			dbses=(session)lsses.get(0);
		}
		return dbses;
	}

	private sessionobj get_session_path(final String p){
		final DbTransaction tn=Db.currentTransaction();
		final List<DbObject> ls=tn.get(sessionobj.class,new Query(session.sessionId,Query.EQ,session_id).and(session.objects).and(sessionobj.path,Query.EQ,p),null,null);
		if(ls.isEmpty())
			return get_session().object(p);
		if(ls.size()>1){
			b.log(new RuntimeException("found "+ls.size()+" paths for session "+session_id+" path "+p));
		}
		return (sessionobj)ls.get(0);
	}

	/** Called from the request thread. */
	void run_page() throws Throwable{
		st=state.run_page;

		final Class<?> cls=b.get_class_for_path(path_str);
		if(cls==null){
			final xwriter x=new xwriter().p(path_str).nl().nl().pl("Resource not found.");
			reply(h_http404,null,null,tobytes(x.toString()));
			b.log(new RuntimeException("http404 "+path()));
			return;
		}

		if(!set_session_id_from_cookie()){
			session_id=make_new_session_id();
			session_id_set=true;
//			pl("new session "+session_id);
			thdwatch.sessions++;
		}

		if(websock.class.isAssignableFrom(cls)){// start a socket
//			System.out.println("request "+Integer.toHexString(hashCode())+": socket at "+path_str);
			st=state.sock;
			websock=(websock)cls.getConstructor().newInstance();
			bb.position(ba_pos); // set the position to the end of processed data. the buffer will be used by websock.
			websock.init(this);
			thdwatch.socks++;
			return;
		}

		if(content_bb!=null){
			populate_content_map_from_buffer();
		}

		final boolean is_stateless=cls.getAnnotation(stateless.class)!=null;

		final DbTransaction tn=is_stateless?null:Db.initCurrentTransaction();
//		System.out.println("dbo: connection pool: "+Db.instance().getConnectionPoolSize());
		try{
			run_page_do(tn,cls);
		}catch(Throwable t){
			if(!is_stateless){
				tn.rollback();
			}
			while(t.getCause()!=null){
				t=t.getCause();
			}
			if(t instanceof RuntimeException)
				throw(RuntimeException)t;
			throw new RuntimeException(t);
		}finally{
			if(!is_stateless){
				Db.deinitCurrentTransaction();
			}
		}

		st=state.next_request;
	}

	/** Called from run_page(). */
	private void run_page_do(final DbTransaction tn,final Class<?> cls) throws Throwable{
		a root_elem;
		final sessionobj dbo_root_elem;
		if(tn==null){
			dbo_root_elem=null;
			root_elem=(a)cls.getConstructor().newInstance();
		}else{
			dbo_root_elem=get_session_path(path_str);
			root_elem=(a)dbo_root_elem.object();
			if(root_elem==null){
				root_elem=(a)cls.getConstructor().newInstance();
			}
		}
		if(!content.isEmpty()){
			// ajax post
			String ajax_command_string="";
			for(final Map.Entry<String,String> me:content.entrySet()){
				if(axfld.equals(me.getKey())){
					ajax_command_string=me.getValue();
					continue;
				}
				// ? indexofloop
				final String[] paths=me.getKey().split(a.id_path_separator);
				a e=root_elem;
				for(int n=1;n<paths.length;n++){
					e=e.child(paths[n]);
					if(e==null)
						throw new RuntimeException("not found: "+me.getKey());
				}
				e.set(me.getValue());
			}
			if(ajax_command_string.length()==0)
				throw new RuntimeException("expectedax");

			// decode the field id, method name and parameters parameters
			final String target_elem_id,target_elem_method,target_elem_method_args;
			final int i1=ajax_command_string.indexOf(' ');
			if(i1==-1){
				target_elem_id=ajax_command_string;
				target_elem_method=target_elem_method_args="";
			}else{
				target_elem_id=ajax_command_string.substring(0,i1);
				final int i2=ajax_command_string.indexOf(' ',i1+1);
				if(i2==-1){
					target_elem_method=ajax_command_string.substring(i1+1);
					target_elem_method_args="";
				}else{
					target_elem_method=ajax_command_string.substring(i1+1,i2);
					target_elem_method_args=ajax_command_string.substring(i2+1);
				}
			}
			// navigate to the target element
			final String[] path=target_elem_id.split(a.id_path_separator);// ? indexofloop
			a target_elem=root_elem;
			for(int n=1;n<path.length;n++){
				target_elem=target_elem.child(path[n]);
				if(target_elem==null){
					break;
				}
			}
			final oschunked os=reply_chunked(h_http200,text_html_utf8);
			final xwriter x=new xwriter(os);
			if(target_elem==null){
				x.xalert("element not found:\n"+target_elem_id);
				os.finish();
				return;
			}
			// invoke method on target element with arguments
			// ! try to find method recursively
			try{
				target_elem.getClass().getMethod("x_"+target_elem_method,xwriter.class,String.class).invoke(target_elem,x,target_elem_method_args);
			}catch(final InvocationTargetException t){
				b.log(t.getTargetException());
				x.closeUpdateIfOpen();
				x.xalert(t.getTargetException().getMessage());
			}catch(final NoSuchMethodException t){
				x.xalert("method not found:\n"+target_elem.getClass().getName()+".x_"+target_elem_method+"(xwriter,String)");
			}
			if(tn!=null){
				dbo_root_elem.object(root_elem);// dbo element is now dirty
				tn.flush();
			}
			x.finish();
			os.finish();
			return;
		}
		final boolean is_binary_producing_elem=root_elem instanceof bin;
		final oschunked os=reply_chunked(h_http200,is_binary_producing_elem?((bin)root_elem).content_type():text_html_utf8);
		final xwriter x=new xwriter(os);
		if(!is_binary_producing_elem){
			os.write(ba_page_header_pre_title);
		}
		try{
			// ? extra mode: serialize, encode to text, write into tag <div id="--state">
			// that is posted with ajax request
			root_elem.to(x);
		}catch(final Throwable t){
			b.log(t);
			x.pre().p(b.stacktrace(t));
		}
		if(tn!=null){
			dbo_root_elem.object(root_elem);
			tn.flush();
		}
		x.finish();
		os.finish();
	}

	private oschunked reply_chunked(final byte[] hdr,final String content_type) throws Throwable{
		final ByteBuffer[] bb_reply=new ByteBuffer[11];
		int bbi=0;
		bb_reply[bbi++]=ByteBuffer.wrap(hdr);
		if(session_id_set){
			bb_reply[bbi++]=ByteBuffer.wrap(hk_set_cookie);
			bb_reply[bbi++]=ByteBuffer.wrap(session_id.getBytes());
			bb_reply[bbi++]=ByteBuffer.wrap(hkv_set_cookie_append);
//			session_id_set=false;
		}
		if(connection_keep_alive){
			bb_reply[bbi++]=ByteBuffer.wrap(hkp_connection_keep_alive);
		}
		if(content_type!=null){
			bb_reply[bbi++]=ByteBuffer.wrap(h_content_type);
			bb_reply[bbi++]=ByteBuffer.wrap(content_type.getBytes());
		}
		bb_reply[bbi++]=ByteBuffer.wrap(hkp_transfer_encoding_chunked);
		bb_reply[bbi++]=ByteBuffer.wrap(ba_crlf2);
//		thdwatch.output+=send_packet(bb_reply,bbi);// ? sends 2 packets. initiate oschuncked with reply buffers to send header at first write
		return new oschunked(this,bb_reply,bbi,b.chunk_B); // ?
	}

	private void populate_content_map_from_buffer() throws Throwable{
//		System.out.println("*** content type: "+content_type);
//		System.out.println(new String(content_bb.array(),0,content_bb.limit()));
		if(content_type!=null&&!content_type.startsWith(text_plain))
			throw new RuntimeException("postedcontent only "+text_plain+" allowed");
		final byte[] ba=content_bb.array();
		int i=0;
		String name="";
		int s=0;
		int k=0;
		for(final byte c:ba){
			switch(s){
			default:
				throw new RuntimeException();
			case 0:
				if(c=='='){
					name=new String(ba,i,k-i,b.strenc);
					i=k+1;
					s=1;
				}
				break;

			case 1:
				if(c=='\r'){
					final String value=new String(ba,i,k-i,b.strenc);
					content.put(name,value);
					i=k+1;
					s=0;
				}
				break;
			}
			k++;
		}
	}

	// ? separation of concerns, this request is a sock or a 'a'
	boolean is_sock(){
		return st==state.sock;
	}

	boolean is_oschunked_waiting_write(){
		return oschunked_waiting_write;
	}

	void oschunked_waiting_write(final boolean b){
		oschunked_waiting_write=b;
	}

	boolean is_connection_keepalive(){
		return connection_keep_alive;
	}

	boolean is_transfer(){
		return st==state.transfer_file||st==state.transfer_buffers;
	}

	boolean is_waiting_run_page(){
		return st==state.waiting_run_page;
	}

	void close(){
		if(is_sock()){
			try{
				websock.on_closed();
			}catch(final Throwable t){
				b.log(t);
			}
		}

		try{
			socket_channel.close();
		}catch(final Throwable t){
			b.log(t);
		}
	}

	boolean is_buffer_empty(){
		return ba_rem==0;
	}

	public InetAddress ip(){
		return socket_channel.socket().getInetAddress();
	}

	public String host(){
		final String h=headers.get("host");
		final String[] ha=h.split(":");
		return ha[0];
	}

	// ? default is port 80?
	public int port(){
		final String h=headers.get("host");
		final String[] ha=h.split(":");
		if(ha.length<2)
			return 80; // ? SSL?
		return Integer.parseInt(ha[1]);
	}

	public String path(){
		return path_str;
	}

	public String query(){
		return query_str;
	}

	public Map<String,String> headers(){
		return headers;
	}

	@Override public String toString(){
		return hashCode()+"\n"+new String(bb.array(),bb.position(),bb.remaining());
//		return new String(ba,ba_pos,ba_rem)+(content_bb==null?"":new String(content_bb.slice().array()));
	}

	public static req get(){
		return ((thdreq)Thread.currentThread()).r;
	}

	public static long file_and_resource_cache_size_B(){
		if(file_and_resource_cache==null)
			return 0;
		long k=0;
		// ? sync(cachef)
		for(final chdresp e:file_and_resource_cache.values()){
			k+=e.byte_buffer().capacity();
		}
		return k;
	}
//	public static long cacheu_size(){
//		if(cacheu==null)return 0;
//		//? sync(cacheu)
//		long k=0;
//		for(final chdresp e:cacheu.values()){
//			if(e.byte_buffer()==null)continue;
//			k+=e.byte_buffer().capacity();
//		}
//		return k;
//	}

	public String session_id(){
		return session_id;
	}

	SelectionKey selection_key;
	SocketChannel socket_channel;
	private state st=state.method;
	final ByteBuffer bb=ByteBuffer.allocate(b.reqinbuf_B);
	private byte[] ba;
	private int ba_rem;
	private int ba_pos;
	private boolean connection_keep_alive;
	private int method_length;
	private final StringBuilder uri_sb=new StringBuilder(128);
	private int uri_length;
	private String path_str;
	private String query_str;
	private path path;
	private int prot_length;
	private int header_name_length;
	private final StringBuilder header_name_sb=new StringBuilder(32);
	private @conf int header_value_length;
	private final StringBuilder header_value_sb=new StringBuilder(128);
	private int headers_count;
	private final Map<String,String> headers=new HashMap<String,String>();
	private String session_id;
	boolean session_id_set;
	private ByteBuffer content_bb;
	private String content_type;
	private long content_remaining_to_read;
	private final HashMap<String,String> content=new HashMap<String,String>();
	private ByteBuffer[] transfer_buffers;
	private long transfer_buffers_remaining;
	private FileChannel transfer_file_channel;
	private long transfer_file_position;
	private long transfer_file_remaining;
	private boolean oschunked_waiting_write;
	private path upload_path;
	private FileChannel upload_channel;
	private String upload_lastmod_s;
	websock websock;
	public static @conf int abuse_method_len=5;
	public static @conf int abuse_uri_len=512;
	public static @conf int abuse_prot_len=11;
	public static @conf int abuse_header_name_len=32;
	public static @conf int abuse_header_value_len=256;
	public static @conf int abuse_header_count=32;
	public static @conf long abuse_upload_len=16*b.G;
	public static @conf long abuse_content_len=1*b.M;

	private static Map<String,chdresp> file_and_resource_cache;

//	private static Map<String,chdresp>cacheu;
	static void init_static(){
		if(b.cache_files){
			file_and_resource_cache=Collections.synchronizedMap(new LinkedHashMap<String,chdresp>(b.cache_files_hashlen));
//		if(b.cache_uris)cacheu=Collections.synchronizedMap(new LinkedHashMap<String,chdresp>());
		}
	}

	final static byte[] h_http200="HTTP/1.1 200 OK".getBytes();
	final static byte[] h_content_length="\r\nContent-Length: ".getBytes();
	final static byte[] h_last_modified="\r\nLast-Modified: ".getBytes();
	final static byte[] h_etag="\r\nETag: ".getBytes();
	final static byte[] h_content_type="\r\nContent-Type: ".getBytes();
	final static byte[] hkp_connection_keep_alive="\r\nConnection: Keep-Alive".getBytes();
	final static byte[] ba_crlf2="\r\n\r\n".getBytes();
	private final static String axfld="$";
	private final static byte[] h_http204="HTTP/1.1 204 No Content".getBytes();
	private final static byte[] h_http206="HTTP/1.1 206 Partial Content".getBytes();
	private final static byte[] h_http304="HTTP/1.1 304 Not Modified".getBytes();
	final static byte[] h_http403="HTTP/1.1 403 Forbidden".getBytes();
	private final static byte[] h_http404="HTTP/1.1 404 Not Found".getBytes();
	final static byte[] hk_set_cookie="\r\nSet-Cookie: i=".getBytes();
	final static byte[] hkv_set_cookie_append=";path=/;expires=Thu, 31-Dec-2099 00:00:00 GMT;SameSite=Lax".getBytes();
	private final static byte[] hkp_transfer_encoding_chunked="\r\nTransfer-Encoding: chunked".getBytes();
	private final static byte[] hkp_accept_ranges_byte="\r\nAccept-Ranges: bytes".getBytes();
	private final static byte[] hk_content_range_bytes="\r\nContent-Range: bytes ".getBytes();
	private final static String hk_connection="connection";
	private final static String hk_content_length="content-length";
	private final static String hk_content_type="content-type";
	private final static String hk_cookie="cookie";
	private final static String hk_if_none_match="if-none-match";
	private final static String hv_keep_alive="keep-alive";
	private final static String s_equals="=";
	private final static String s_minus="-";
	private final static String s_range="range";
	private final static String s_slash="/";
	private final static byte[] ba_page_header_pre_title="<!doctype html><meta name=viewport content=\"width=device-width,initial-scale=1\"><meta charset=utf-8><link rel=stylesheet href=/x.css><script src=/x.js></script>".getBytes();
	private final static String text_html_utf8="text/html;charset=utf-8";
	private final static String text_plain="text/plain";// ? utf8 encoding?
//	private final static String text_plain_utf8="text/plain;charset=utf-8";
	enum state{
		next_request,method,uri,prot,header_name,header_value,content_read,transfer_file,transfer_buffers,waiting_run_page,run_page,content_upload,sock
	}
}
