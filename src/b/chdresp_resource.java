package b;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/** Cached resource. */
final class chdresp_resource extends chdresp {
	/** Caches a resource. */
	public chdresp_resource(final InputStream is, final byte[] content_type) throws Throwable {
		this.content_type = content_type;
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		b.cp(is, os, null);
		final byte[] ba = os.toByteArray();
		content_length_in_bytes = ba.length;
		// prepare the cached buffer
		bb = ByteBuffer.allocateDirect(hdrlencap + content_length_in_bytes);
		bb.put(req.h_http200);
		bb.put(req.h_content_length).put(Integer.toString(content_length_in_bytes).getBytes());
		etag = b.resources_etag;
		bb.put(req.h_etag).put(etag.getBytes());
		bb.put(req.hkp_connection_keep_alive);
		if (content_type != null) {
			bb.put(req.h_content_type);
			bb.put(content_type);
		}
		additional_headers_insertion_position = bb.position();
		bb.put(req.ba_crlf2);
		content_position = bb.position();
		bb.put(ba);
		bb.flip();
	}

	/** @return true if path is valid, false to evict it from the cache */
	@Override
	boolean validate(final long now) throws Throwable {
		return true; // resources are always up to date
	}
}
