package b;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
final class chdresp_resource extends chdresp{ // ? mixes concerns. try interface of inheritance.
	/** Caches a resource. */
	public chdresp_resource(final InputStream is,final String contentType)throws Throwable{
		this.contentType=contentType;
		final ByteArrayOutputStream os=new ByteArrayOutputStream();
		b.cp(is,os,null);
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
		content_position=bb.position();
		bb.put(ba);
		bb.flip();
	}
	/** @return true if path is valid, false to evict it from the cache */
	boolean validate(final long now)throws Throwable{
		return true; // resources are always up to date
	}
}
