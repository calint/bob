package b;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
final class oschunked extends OutputStream{
	private static final ByteBuffer bb_eochunk=ByteBuffer.wrap("0\r\n\r\n".getBytes());
	private static final ByteBuffer bb_crnl=ByteBuffer.wrap("\r\n".getBytes());
	private final req r;
	private final int chunk_size_bytes;
	private final byte[]chunkhx;
	private final byte[]buf;
	private int bufi;
	oschunked(final req r,final int chunk_size_bytes){this.r=r;this.chunk_size_bytes=chunk_size_bytes;chunkhx=(Integer.toHexString(chunk_size_bytes)+"\r\n").getBytes();buf=new byte[chunk_size_bytes];}
	public String toString(){return new String(buf,0,bufi);}
	public void write(final int ch)throws IOException{throw new UnsupportedOperationException();}
	public void write(final byte[]b)throws IOException{write(b,0,b.length);}
	public void write(final byte[]c,int off,int len)throws IOException{
		final int remain=buf.length-bufi;
		if(len<=remain){
			System.arraycopy(c,off,buf,bufi,len);
			bufi+=len;
			return;
		}
		System.arraycopy(c,off,buf,bufi,remain);
		bufi+=remain;off+=remain;len-=remain;
		final ByteBuffer[]bba=new ByteBuffer[]{ByteBuffer.wrap(chunkhx),ByteBuffer.wrap(buf,0,bufi),bb_crnl.slice()};
		write_blocking(bba);
		while(len>chunk_size_bytes){
			final ByteBuffer[]bba2=new ByteBuffer[]{ByteBuffer.wrap(chunkhx),ByteBuffer.wrap(c,off,chunk_size_bytes),bb_crnl.slice()};
			write_blocking(bba2);
			off+=chunk_size_bytes;
			len-=chunk_size_bytes;
		}
		if(len>0){
			System.arraycopy(c,off,buf,0,len);
			bufi=len;
		}
	}
	private void write_blocking(final ByteBuffer[]bba)throws IOException{
		long remaining=0;for(final ByteBuffer bb:bba)remaining+=bb.remaining();
		while(remaining!=0){
			final long c=r.sockch.write(bba,0,bba.length);//?
			if(c==0)synchronized(r){
				r.waiting_write(true);
				r.selkey.interestOps(SelectionKey.OP_WRITE);
				r.selkey.selector().wakeup();//?? racing
					/*;;*/try{r.wait();}catch(final InterruptedException ok){}
				r.waiting_write(false);
			}
			remaining-=c;thdwatch.output+=c;
		}
	}
	public void flush()throws IOException{
		if(bufi==0)return;
		final ByteBuffer[]bba=new ByteBuffer[]{ByteBuffer.wrap(Integer.toHexString(bufi).getBytes()),bb_crnl.slice(),ByteBuffer.wrap(buf,0,bufi),bb_crnl.slice()};
		write_blocking(bba);
		bufi=0;
	}
	void finish()throws IOException{flush();write_blocking(new ByteBuffer[]{bb_eochunk.slice()});}
}
