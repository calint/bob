package b;
import java.nio.ByteBuffer;
abstract class chdresp{ // ? mixes concerns. try interface of inheritance.
	static final int hdrlencap=8*64;
	String etag;
	byte[] content_type;
	int content_length_in_bytes;
	ByteBuffer bb;
	int additional_headers_insertion_position;
	int content_position;

	final boolean etag_matches(String clientetag){
		return etag.equals(clientetag);
	}
	final ByteBuffer byte_buffer(){
		return bb;
	}
	/** @return where in the buffer to insert headers. */
	final int additional_headers_insertion_position(){
		return additional_headers_insertion_position;
	}
	/** @return where in the buffer start of content is. */
	final int content_position(){
		return content_position;
	}
	final int content_length_in_bytes(){
		return content_length_in_bytes;
	}
	byte[] content_type(){
		return content_type;
	}
	/** @return true if is valid, false to evict from the cache. */
	abstract boolean validate(final long now) throws Throwable;
}
