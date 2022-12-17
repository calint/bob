package b;
import java.io.IOException;
import java.io.OutputStream;
final class osjsstr extends OutputStream{
	private static final byte[] b_jsstr_sq="\\'".getBytes();
	private static final byte[] b_jsstr_cr="\\r".getBytes();
	private static final byte[] b_jsstr_nl="\\n".getBytes();
	private static final byte[] b_jsstr_bs="\\\\".getBytes();
	private static final byte[] b_jsstr_eof="\\0".getBytes();
	private final OutputStream os;
	public osjsstr(final OutputStream os){
		this.os=os;
	}
	@Override public void write(final int ch) throws IOException{
		throw new UnsupportedOperationException();
	}
	@Override public void write(final byte[] c) throws IOException{
		write(c,0,c.length);
	}
	@Override public void write(final byte[] c,final int off,final int len) throws IOException{
		int i=0;
		for(int n=0;n<len;n++){
			final byte b=c[off+n];
			if(b=='\n'){
				final int k=n-i;
				if(k!=0){
					os.write(c,off+i,k);
				}
				os.write(b_jsstr_nl);
				i=n+1;
			}else if(b=='\r'){
				final int k=n-i;
				if(k!=0){
					os.write(c,off+i,k);
				}
				os.write(b_jsstr_cr);
				i=n+1;
			}else if(b=='\''){
				final int k=n-i;
				if(k!=0){
					os.write(c,off+i,k);
				}
				os.write(b_jsstr_sq);
				i=n+1;
			}else if(b=='\\'){
				final int k=n-i;
				if(k!=0){
					os.write(c,off+i,k);
				}
				os.write(b_jsstr_bs);
				i=n+1;
			}else if(b=='\0'){
				final int k=n-i;
				if(k!=0){
					os.write(c,off+i,k);
				}
				os.write(b_jsstr_eof);
				i=n+1;
			}
		}
		final int k=len-i;
		if(k!=0){
			os.write(c,off+i,k);
		}
	}
	@Override public void flush() throws IOException{
		os.flush();
	}
	@Override public String toString(){
		return os.toString();
	}

}
