package b;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
public interface sock{
	enum op{write,read,close,noop,wait}
	op sockinit(final Map<String,String>hdrs,final SocketChannel sc,final ByteBuffer bbspil)throws Throwable;
	op read()throws Throwable;
	op write()throws Throwable;
	void onconnectionlost()throws Throwable;
}
