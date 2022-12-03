package b;
import java.nio.ByteBuffer;
final class chdresp_file extends chdresp{
	private final path path;
	private long lastModified;
	private long last_validation_time;

	/** Caches a file. */
	chdresp_file(final path p,final byte[] content_type) throws Throwable{
		path=p;
		this.content_type=content_type;
		validate(System.currentTimeMillis());
	}

	/** @return true if path is valid, false to evict it from the cache */
	boolean validate(final long now) throws Throwable{
		if(now-last_validation_time<b.cache_files_validate_dt) // only check once every cache_files_validate_dt ms
			return true;

		last_validation_time=now;
		if(!path.exists()) return false;// file is gone
		final long path_lastModified=path.lastmod();
		if(path_lastModified==lastModified) return true;// file is up to date
		// file needs to be refreshed
		content_length_in_bytes=(int)path.size();
		if(content_length_in_bytes>b.cache_files_maxsize) return false;// file has changed and is now to big

		// build cached buffers
		bb=ByteBuffer.allocateDirect(hdrlencap+content_length_in_bytes);
		bb.put(req.h_http200);
		bb.put(req.h_content_length).put(Long.toString(content_length_in_bytes).getBytes());
		if(content_type!=null) bb.put(req.h_content_type).put(content_type);
		etag="\""+path_lastModified+"\"";
		bb.put(req.h_etag).put(etag.getBytes());
		bb.put(req.hkp_connection_keep_alive);
		additional_headers_insertion_position=bb.position();
		bb.put(req.ba_crlf2);
		content_position=bb.position();
		path.to(bb);
		bb.flip();
		lastModified=path_lastModified;
		return true;
	}
}
