package b;
import java.io.*;
public final class osltgt extends OutputStream{
	private static final byte[]ba_html_gt="&gt;".getBytes();
	private static final byte[]ba_html_lt="&lt;".getBytes();
	private final OutputStream os;
	public osltgt(final OutputStream os){
		this.os=os;
	}
	public void write(final int ch)throws IOException{throw new UnsupportedOperationException();}
	public void write(final byte[]c)throws IOException{write(c,0,c.length);}
	public void write(final byte[]c,int off,int len)throws IOException{
		int i=0;
		for(int n=0;n<len;n++){
			byte b=c[off+n];
			if(b=='<'){
				final int l=n-i;
				if(l!=0)
					os.write(c,off+i,l);
				os.write(ba_html_lt);
				i=n+1;
			}else if(b=='>'){
				final int l=n-i;
				if(l!=0)
					os.write(c,off+i,l);
				os.write(ba_html_gt);
				i=n+1;
			}
		}
		final int l=len-i;
		if(l!=0)
			os.write(c,off+i,l);
	}
	@Override public void flush()throws IOException{os.flush();}
}
