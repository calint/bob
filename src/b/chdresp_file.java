package b;
import java.nio.ByteBuffer;
final class chdresp_file extends chdresp{
	private path path;
	private long lastModified;
	private long last_validation_time;
	
	/** Caches a file. */
	chdresp_file(final path p)throws Throwable{
		path=p;
		validate(System.currentTimeMillis());
	}
	
	/** @return true if path is valid, false to evict it from the cache */
	boolean validate(final long now)throws Throwable{
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
		data_position=bb.position();
		path.to(bb);
//		content_length_in_bytes=bb.position()-i0;
		bb.flip();
		lastModified=path_lastModified;
		return true;
	}
}
