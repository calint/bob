package b;
import java.io.*;
public class osnl extends OutputStream{//? refactor
	private final StringBuilder line=new StringBuilder(256);
	final public void write(final int c)throws IOException{throw new UnsupportedOperationException();}
	final public void write(final byte[]c) throws IOException{write(c,0,c.length);}
	final public void write(final byte[]c,final int off,final int len)throws IOException{
		for(int n=0;n<len;n++){
			final byte b=c[off+n];
			if(b=='\n'){
				try{onnewline(line.toString());}catch(Throwable e){throw new Error(e);}
				line.setLength(0);
			}else
				line.append((char)b);
		}
	}
	public void onnewline(final String line)throws Throwable{System.out.println(line);}
}
