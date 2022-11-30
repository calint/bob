package b;
/** Cacheable instances of 'a' implement this interface. */
public interface cacheable{
	String filetype();
	String contenttype();
	String etag();
	long lastmodupdms();
	boolean cacheforeachuser();
}