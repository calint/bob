package b;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
final class chdresp{ // ? mixes concerns. try interface of inheritance.
	private static final int hdrlencap=8*64;
	private String etag;
	private String contentType;
	private int content_length_in_bytes;
	private ByteBuffer bb;
	private int additional_headers_insertion_position;
	private int data_position;
	
	private boolean isresource;
	private path path;
	private long lastModified;
	private long last_validation_time;
	
	/** Caches a file. */
	chdresp(final path p)throws Throwable{
		path=p;
		validate(System.currentTimeMillis());
	}
//	chdresp(final cacheable c,final String key){cacheable=c;this.key=key;dt=c.lastmodupdms();}
//	chdresp(final cacheable c,final String key){cacheable=c;this.key=key;}
	/** Caches a resource. */
	public chdresp(final InputStream is,final String contentType)throws IOException{
		isresource=true;
		this.contentType=contentType;
		final ByteArrayOutputStream os=new ByteArrayOutputStream();
		b.cp(is,os);
		final byte[]ba=os.toByteArray();
		content_length_in_bytes=ba.length;
		// prepare the cached buffer
		bb=ByteBuffer.allocateDirect(hdrlencap+content_length_in_bytes);
		bb.put(req.h_http200);
		bb.put(req.h_content_length).put(Integer.toString(content_length_in_bytes).getBytes());
		etag=b.resources_etag;
		bb.put(req.h_etag).put(etag.getBytes());
		bb.put(req.hkp_connection_keep_alive);
		if(contentType!=null) {
			bb.put(req.h_content_type);
			bb.put(contentType.getBytes());
		}
		additional_headers_insertion_position=bb.position();
		bb.put(req.ba_crlf2);
		data_position=bb.position();
		bb.put(ba);
		bb.flip();
	}
//	boolean ifnotmodsince(final String ifModSince){
//		return ifModSince.equals(lastModified_s); // ? check lastModified before ifModSince
//	}
	boolean etag_matches(final String clientetag) {
		return etag.equals(clientetag);
	}
	ByteBuffer byteBuffer(){return bb;}
	int additional_headers_insertion_position(){return additional_headers_insertion_position;}
	int data_position(){return data_position;}
	int content_length_in_bytes(){return content_length_in_bytes;}
	String contentType(){return contentType;}
//	String lastModified(){return lastModified_s;}
	
	/** @return true if path is valid, false to evict it from the cache */
	boolean validate(final long now)throws Throwable{
		if(isresource)
			return true; // resources are always up to date
		
		if(now-last_validation_time<b.cache_files_validate_dt) // only check once every cache_files_validate_dt ms
			return true;

		last_validation_time=now;
		if(!path.exists())return false;// file is gone
		final long path_lastModified=path.lastmod();
		if(path_lastModified==lastModified)return true;// file is up to date
		// file needs to be refreshed
		content_length_in_bytes=(int)path.size();
		if(content_length_in_bytes>b.cache_files_maxsize)return false;// file has changed and is now to big
		
		// build cached buffers
		bb=ByteBuffer.allocateDirect(hdrlencap+content_length_in_bytes);
		bb.put(req.h_http200);
		bb.put(req.h_content_length).put(Long.toString(content_length_in_bytes).getBytes());
		if(contentType!=null) // todo set content type depending on suffix
			bb.put(req.h_content_type).put(contentType.getBytes());
		etag="\""+path_lastModified+"\"";
		bb.put(req.h_etag).put(etag.getBytes());
		bb.put(req.hkp_connection_keep_alive);
		additional_headers_insertion_position=bb.position();
		bb.put(req.ba_crlf2);
//		final int i0=bb.position();
		path.to(bb);
//		content_length_in_bytes=bb.position()-i0;
		bb.flip();
		lastModified=path_lastModified;
		return true;
	}
//	private void validatecacheable(final long now,final String clientetag)throws Throwable{
//		if(isvalid(now))
//			return;
//		ts=now;
//		contentType=cacheable.contenttype();
////		lastModified_s=cacheable.lastmod();
////		if(lastModified_s==lm)return;
////		if(lastModified_s!=null&&lastModified_s.equals(lm))return;
//		etag=cacheable.etag();
//		if(etag!=null&&etag.equals(clientetag))
//			return;
//		final ByteArrayOutputStream baos=new ByteArrayOutputStream(b.io_buf_B);
//		((a)cacheable).to(new xwriter(baos));
//		final byte[]ba=baos.toByteArray();
//		//? consider byte ranges
//		if(b.cacheu_tofile)b.path(b.cacheu_dir+key+"."+cacheable.filetype()).writebb(ByteBuffer.wrap(ba));
//		bb=ByteBuffer.allocate(256+ba.length);//? calcsize
//		bb.put(req.h_http200);
//		bb.put(req.h_content_length).put(Long.toString(baos.size()).getBytes());
////		if(lastModified_s!=null)
////			bb.put(req.h_last_modified).put(lastModified_s.getBytes());
//		bb.put(req.h_etag).put(etag.getBytes());
//		if(contentType!=null) {
//			bb.put(req.h_content_type).put(contentType.getBytes());
//		}
//		bb.put(req.hkp_connection_keep_alive);
//		additional_headers_insertion_position=bb.position();
//		bb.put(req.ba_crlf2);
//		data_position=bb.position();
//		final int i0=bb.position();
//		bb.put(ba);
//		content_length_in_bytes=bb.position()-i0;
//		bb.flip();
//	}
//	boolean isvalid(final long now){return now-ts<cacheable.lastmodupdms();}
}
