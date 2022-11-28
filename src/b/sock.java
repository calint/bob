package b;
import java.util.*;
public interface sock{
	enum op{write,read,close,noop,wait}
	op sockinit(final Map<String,String>hdrs,final sockio so)throws Throwable;
	op read()throws Throwable;
	op write()throws Throwable;
	void onconnectionlost()throws Throwable;
}
