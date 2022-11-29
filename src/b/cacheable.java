package b;
/** Cacheable instances of 'a' implement this interface. */
public interface cacheable{
	String filetype();
	String contenttype();
	String lastmod();
	long lastmodupdms();
	boolean cacheforeachuser();
}