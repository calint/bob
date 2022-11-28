package b;
import java.nio.*;
import java.security.*;
import java.util.*;
import static b.b.*;
public class websock extends a implements sock{static final long serialVersionUID=1;
	private sockio so;
	private ByteBuffer bbi;
	private static enum state{closed,handshake,read_next_frame,read_continue}
	private state st=state.closed;
	private final byte[]maskkey=new byte[4];
	private int payloadlendec;
	private ByteBuffer[]bbos;
	private session session;
	private boolean firstpak;
	final public op sockinit(final Map<String,String>hdrs,final sockio so)throws Throwable{
		this.so=so;
		bbi=so.inbuf();
		session=req.get().session();
		st=state.handshake;
		// rfc6455#section-1.3
		// Opening Handshake
//		if(!"13".equals(hdrs.get("sec-websocket-version")))throw new Error("sec-websocket-version not 13");
		System.out.println("@@@@@ 1:   "+hdrs);
		final String key=hdrs.get("sec-websocket-key");
		final String s=key+"258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		final byte[]sha1ed=MessageDigest.getInstance("SHA-1").digest(s.getBytes());
		final String replkey=base64.encodeToString(sha1ed,true);
		final ByteBuffer bbo=ByteBuffer.allocate(b.K>>2);
//		final String prot=hdrs.get("sec-webSocket-protocol");
		bbo.put("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: ".getBytes());
		bbo.put(replkey.getBytes());
//		bbo.put("\r\nSec-WebSocket-Protocol: chat".getBytes());
		bbo.put("\r\n\r\n".getBytes());
		bbo.flip();
		System.out.println("@@@@@ 2:   "+new String(bbo.array(),"utf8"));
//		System.out.println(hdrs);
//		System.out.println(new String(bbo.array(),0,bbo.limit()));
		while(bbo.hasRemaining()&&0!=so.write(bbo));if(bbo.hasRemaining())throw new Error("packetnotfullysent");
		bbi.position(bbi.limit());
		st=state.read_next_frame;
		onopened();
		return op.read;
	}
	protected final void enableclientinput(){so.reqread();}
	protected void onopened()throws Throwable{}
	final public op read()throws Throwable{
		if(!bbi.hasRemaining()){
			bbi.clear();
			final int n=so.read(bbi);
			if(n==0)return op.read;//? infloop?
			if(n==-1){
				st=state.closed;
//				onclosed(); //? onclosed called when request closed
				return op.close;
			}
			bbi.flip();
		}
		while(true)switch(dobbi()){default:throw new Error();
			case read:if(bbi.hasRemaining())continue;return op.read;
			case write:return op.write;
			case close:return op.close;
		}		
	}
	@Override public void onconnectionlost()throws Throwable{
		onclosed();}
	protected void onclosed()throws Throwable{}
	private int maskc;
	final private op dobbi()throws Throwable{
		switch(st){default:throw new Error();
		case read_next_frame:
			// rfc6455#section-5.2
			// Base Framing Protocol
			final int b0=(int)bbi.get();
			final boolean fin=(b0&128)==128;
			if(fin);// to remove warning of unused variable
			final int resv=(b0>>4)&7;
			if(resv!=0)throw new Error("reserved bits are not 0");
			final int opcode=b0&0xf;
			if(opcode==8){// rfc6455#section-5.5.1
				st=state.closed;
//				onclosed();   // onclose called when req closed
				return op.close;
			}
			final int b1=(int)bbi.get();
			final boolean masked=(b1&128)==128;
			if(!masked)throw new Error("unmasked client message");
			int payloadlen=b1&127;
			if(payloadlen==126){
				final int by2=(((int)bbi.get()&0xff)<<8);
				final int by1= ((int)bbi.get()&0xff);
				payloadlen=by2|by1;
			}else if(payloadlen==127){
				bbi.get();bbi.get();bbi.get();bbi.get();
				final int by4=(((int)bbi.get()&0xff)<<24);
				final int by3=(((int)bbi.get()&0xff)<<16);
				final int by2=(((int)bbi.get()&0xff)<<8);
				final int by1= ((int)bbi.get()&0xff);
				payloadlen=by4|by3|by2|by1;
			}
			bbi.get(maskkey);
			payloadlendec=payloadlen;
			firstpak=true;
			st=state.read_continue;
			maskc=0;
		case read_continue:
			//demask
			final byte[]bbia=bbi.array();
			final int pos=bbi.position();
			final int limn=bbi.remaining()>payloadlendec?pos+payloadlendec:bbi.limit();
			if(!(maskkey[0]==0&&maskkey[1]==0&&maskkey[2]==0&&maskkey[3]==0)){
				for(int i=pos;i<limn;i++){
					final byte b=(byte)(bbia[i]^maskkey[maskc]);
					bbia[i]=b;
					maskc++;
					maskc%=maskkey.length;
				}
			}
			final int ndata=limn-pos;
			payloadlendec-=ndata;
			if(payloadlendec==0)
				st=state.read_next_frame;
			final ByteBuffer bbii=ByteBuffer.wrap(bbi.array(),pos,ndata);//bbi.slice();
			//bbii.limit(ndata);
			onpayload(bbii,ndata,payloadlendec,firstpak,payloadlendec==0);
			bbi.position(limn);
			firstpak=false;
			return bbos==null?op.read:op.write;
		}
	}
	private ByteBuffer bbrq;
	final private void onpayload(final ByteBuffer bb,final int nbytes,final int payloadlenlft,final boolean firstpak,final boolean lastpak)throws Throwable{
		if(firstpak&&!lastpak){bbrq=ByteBuffer.allocate(nbytes+payloadlenlft);bbrq.put(bb);return;}
		if(!firstpak&&!lastpak){bbrq.put(bb);return;}
		if(!firstpak&&lastpak){bbrq.put(bb);bbrq.flip();}
		if(firstpak&&lastpak){bbrq=bb;}
		onmessage(bbrq);//? wrap so position is 0
		bbrq=null;
	}
	protected void onmessage(final ByteBuffer bb)throws Throwable{}
	final public op write()throws Throwable{
		if(bbos==null){
			System.out.println("bbos is null");
			return op.read;
		}
		so.write(bbos);
		if(bbos==null){
			System.out.println("bbos is null 2");
			return op.read;
		}
		for(final ByteBuffer b:bbos){
			if(b==null){
				System.out.println("b is null");
				return op.read;
			}
			if(b.hasRemaining()){
//				System.out.println("incomplete write");
				return op.write;
			}
		}
		bbos=null;
		return op.read;
	}
	final protected session session(){return session;}
	final public void send_binary(final String s)throws Throwable{
		final ByteBuffer bb=ByteBuffer.wrap(b.tobytes(s));
		send(bb,false);
	}
	final public void send(final ByteBuffer bb)throws Throwable{send(bb,true);}
	final public void send(final ByteBuffer bb,final boolean textmode)throws Throwable{
		if(bbos!=null)throw new Error("overwrite");//?
		// rfc6455#section-5.2
		// Base Framing Protocol
		final int ndata=bb.remaining();
		bbos=new ByteBuffer[]{hdr(ndata,textmode),bb};
		if(write()==op.write)so.reqwrite();
	}

	final public void send(final ByteBuffer[]bba)throws Throwable{send(bba,true);}
	final public void send_binary(final ByteBuffer...bba)throws Throwable{
		send(bba,false);
	}
	final public void send_binary(final byte[]...bb)throws Throwable{
		final ByteBuffer[]bba=new ByteBuffer[bb.length];
		int i=0;
		for(byte[]ba:bb)bba[i++]=ByteBuffer.wrap(ba);
		send(bba,false);
	}
	final public void send_binary(final String...sa)throws Throwable{
		final ByteBuffer[]bba=new ByteBuffer[sa.length];
		int i=0;
		for(String s:sa)bba[i++]=ByteBuffer.wrap(tobytes(s));
		send(bba,false);
	}
	final public void send(final ByteBuffer[]bba,final boolean textmode)throws Throwable{
		if(bbos!=null)throw new Error("overwrite");//?
		int ndata=0;
		for(final ByteBuffer b:bba)ndata+=b.remaining();
		bbos=new ByteBuffer[bba.length+1];
		bbos[0]=hdr(ndata,textmode);
		for(int i=1;i<bbos.length;i++)bbos[i]=bba[i-1];
		if(write()==op.write)so.reqwrite();
	}
	private ByteBuffer hdr(final int ndata,final boolean textmode){
		// rfc6455#section-5.2
		// Base Framing Protocol
		int nhdr;
		final byte[]hdr=new byte[10];
		hdr[0]=(byte)((textmode?1:2)|128);
		if(ndata<=125){
			hdr[1]=(byte)ndata;
			nhdr=2;
		}else if(ndata<=65535){
			hdr[1]=126;
			hdr[2]=(byte)((ndata>>8)&255);
			hdr[3]=(byte)( ndata    &255);
			nhdr=4;
		}else{
			hdr[1]=127;
//			hdr[2]=(byte)((ndata>>56)&255);
//			hdr[3]=(byte)((ndata>>48)&255);
//			hdr[4]=(byte)((ndata>>40)&255);
//			hdr[5]=(byte)((ndata>>32)&255);
			hdr[6]=(byte)((ndata>>24)&255);
			hdr[7]=(byte)((ndata>>16)&255);
			hdr[8]=(byte)((ndata>> 8)&255);
			hdr[9]=(byte)( ndata     &255);
			nhdr=10;
		}
		return ByteBuffer.wrap(hdr,0,nhdr);
	}
	final protected boolean issending(){return bbos!=null;}
}