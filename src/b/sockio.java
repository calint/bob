package b;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
public final class sockio{
	private final SocketChannel sc;
	private final SelectionKey sk;
	private ByteBuffer bbspil;
	sockio(final SocketChannel sc,final SelectionKey sk,final ByteBuffer bbspil){this.sc=sc;this.sk=sk;this.bbspil=bbspil;}
	public ByteBuffer inbuf(){
		final ByteBuffer bb=bbspil;
		bbspil=null;
		return bb;
	}
	public int read(final ByteBuffer bb)throws IOException{
		final int c=sc.read(bb);
		thdwatch.input+=c;
		return c;
	}
	public int write(final ByteBuffer bb)throws IOException{
		final int c=sc.write(bb);
		thdwatch.output+=c;
		return c;
	}
	public long write(final ByteBuffer[]bbs)throws IOException{
		final long c=sc.write(bbs);
		thdwatch.output+=c;
		return c;
	}
	public void reqwrite(){
		sk.interestOps(SelectionKey.OP_WRITE);
		sk.selector().wakeup();
	}
	public void reqread(){
		sk.interestOps(SelectionKey.OP_READ);
		sk.selector().wakeup();
	}
	public void close(){try{sc.close();}catch(Throwable ignored){}}
}
