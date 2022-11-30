package b;
import java.nio.ByteBuffer;
abstract class chdresp{ // ? mixes concerns. try interface of inheritance.
	static final int hdrlencap=8*64;
	String etag;
	String contentType;
	int content_length_in_bytes;
	ByteBuffer bb;
	int additional_headers_insertion_position;
	int data_position;
	
	final boolean etag_matches(String clientetag){return etag.equals(clientetag);}
	final ByteBuffer byteBuffer(){return bb;}
	/** @return where in the buffer to insert headers. */
	final int additional_headers_insertion_position(){return additional_headers_insertion_position;}
	/** @return where in the buffer start of content is. */
	final int data_position(){return data_position;}
	final int content_length_in_bytes(){return content_length_in_bytes;}
	/** @return true if is valid, false to evict from the cache. */
	abstract boolean validate(final long now)throws Throwable;
}