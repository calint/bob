package b;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
public interface sock{
	enum op{write,read,close,noop,wait}
	/** @param bbspil is forwarded by the request to continue processing */
	op sockinit(Map<String,String>hdrs,SocketChannel sc,ByteBuffer bbspil)throws Throwable;
	op read()throws Throwable;
	op write()throws Throwable;
	void onconnectionlost()throws Throwable;
}
