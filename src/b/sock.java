package b;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
public interface sock{
	enum op{
		write,read,close,noop,wait
	}
	/** @param bb is forwarded by the request to continue processing */
	op sock_init(Map<String,String> headers,SocketChannel sc,ByteBuffer bb) throws Throwable;
	op sock_read() throws Throwable;
	op sock_write() throws Throwable;
	void sock_on_closed() throws Throwable;
}
