package b;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Map;
public class websock implements sock{
	private sockio so;
	private ByteBuffer bbi;
	private static enum state{closed,handshake,read_next_frame,read_continue}
	private state st=state.closed;
	private final byte[]maskkey=new byte[4];
	private int payloadlendec;
	private ByteBuffer[]bbos;
//	private session session;
	private boolean firstpak;
	private int maskc;
	private ByteBuffer bbrq;
	final public op sockinit(final Map<String,String>hdrs,final sockio so)throws Throwable{
		this.so=so;
		bbi=so.inbuf();
//		session=req.get().session();
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
		// ? add session cookie
		bbo.put("\r\n\r\n".getBytes());
		bbo.flip();
		System.out.println("@@@@@ 2:   "+new String(bbo.array(),"utf8"));
//		System.out.println(hdrs);
//		System.out.println(new String(bbo.array(),0,bbo.limit()));
		while(bbo.hasRemaining()&&so.write(bbo)!=0);
			if(bbo.hasRemaining())
				throw new RuntimeException("packetnotfullysent");
		bbi.position(bbi.limit());
		st=state.read_next_frame;
		onopened();
		return op.read;
	}
	protected void onopened()throws Throwable{}
	final public op read()throws Throwable{
		if(!bbi.hasRemaining()){
			bbi.clear();
			final int n=so.read(bbi);
			if(n==0)return op.read;//? infloop?
			if(n==-1){
				st=state.closed;
//				onclosed(); //? onclosed called when request is closed
				return op.close;
			}
			bbi.flip();
		}
		while(true) {
			switch(dobbi()){default:throw new Error();
				case read:
					if(bbi.hasRemaining())
						continue;
					return op.read;
				case write:return op.write;
				case close:return op.close;
			}		
		}
	}
	@Override public void onconnectionlost()throws Throwable{
		onclosed();
	}
	
	/** Called when the web socket has been closed. */
	protected void onclosed()throws Throwable{}
	
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
				bbi.get();// skip the bytes that encode a length >4G
				bbi.get();
				bbi.get();
				bbi.get();
				final int by4=(((int)bbi.get()&0xff)<<24);
				final int by3=(((int)bbi.get()&0xff)<<16);
				final int by2=(((int)bbi.get()&0xff)<<8);
				final int by1= ((int)bbi.get()&0xff);
				payloadlen=by4|by3|by2|by1;
			}
			bbi.get(maskkey);
			payloadlendec=payloadlen;
			firstpak=true;
			maskc=0;
			st=state.read_continue;
			// fall through
		case read_continue:
			// unmask
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
			final ByteBuffer bbii=ByteBuffer.wrap(bbi.array(),pos,ndata);// bbi position is start of data
			//bbii.limit(ndata);
			onpayload(bbii,ndata,payloadlendec,firstpak,payloadlendec==0);
			bbi.position(limn);
			firstpak=false;
			return bbos==null?op.read:op.write; // onmessage might have set buffer to send
		}
	}
	
	final private void onpayload(ByteBuffer bb,int nbytes,int payloadlenlft,boolean firstpak,boolean lastpak)throws Throwable{
		if(firstpak&&!lastpak){
			bbrq=ByteBuffer.allocate(nbytes+payloadlenlft);
			bbrq.put(bb);
			return;
		}
		if(!firstpak&&!lastpak){
			bbrq.put(bb);
			return;
		}
		if(!firstpak&&lastpak){
			bbrq.put(bb);
			bbrq.flip();
		}
		if(firstpak&&lastpak){
			bbrq=bb;
		}
		onmessage(bbrq);
		bbrq=null;
	}
	
	/** Called when a message has been decoded. ByteBuffer position is at start of data and limit marks the end of data.
	 *  Note that it is assumed that client does not send a new message prior to receiving a reply. */
	protected void onmessage(ByteBuffer bb)throws Throwable{}

	/** Called by the request or by send(...) */
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
		for(ByteBuffer b:bbos){ // check if the write is complete.
			if(b==null){
				System.out.println("b is null");
				return op.read;
			}
			if(b.hasRemaining()){
				return op.write; // will trigger a new write when called from request
			}
		}
		bbos=null; // will trigger a read request when called from send(...), otherwise write
		return op.read;
	}
//	final protected session session(){return session;}
//	final public void send_binary(String s)throws Throwable{
//		final ByteBuffer bb=ByteBuffer.wrap(b.tobytes(s));
//		send(bb,false);
//	}
	
	final public void send(String s)throws Throwable{
		send(new ByteBuffer[]{ByteBuffer.wrap(s.getBytes())},true);
	}
	
//	final public void send(ByteBuffer bb)throws Throwable{send(bb,true);}
	
	final public void send(ByteBuffer bb,final boolean textmode)throws Throwable{
		if(bbos!=null)throw new Error("overwrite");//?
		// rfc6455#section-5.2
		// Base Framing Protocol
		final int ndata=bb.remaining();
		bbos=new ByteBuffer[]{make_header(ndata,textmode),bb};
//		if(write()==op.write)
//			so.reqwrite();
		write(); // return ignored because bbos will be set to null when write is finished
	}

//	final public void send(final ByteBuffer[]bba)throws Throwable{send(bba,true);}
	
//	final public void send_binary(final ByteBuffer...bba)throws Throwable{
//		send(bba,false);
//	}
	
//	final public void send_binary(final byte[]...bb)throws Throwable{
//		final ByteBuffer[]bba=new ByteBuffer[bb.length];
//		int i=0;
//		for(byte[]ba:bb)bba[i++]=ByteBuffer.wrap(ba);
//		send(bba,false);
//	}
//	final public void send_binary(final String...sa)throws Throwable{
//		final ByteBuffer[]bba=new ByteBuffer[sa.length];
//		int i=0;
//		for(String s:sa)
//			bba[i++]=ByteBuffer.wrap(tobytes(s));
//		send(bba,false);
//	}
	final public void send(final ByteBuffer[]bba,final boolean textmode)throws Throwable{
		if(bbos!=null)throw new Error("overwrite"); // ? is the buffer size enough for most use cases?
		int ndata=0;
		for(final ByteBuffer b:bba)
			ndata+=b.remaining();
		bbos=new ByteBuffer[bba.length+1];
		bbos[0]=make_header(ndata,textmode);
		for(int i=1;i<bbos.length;i++)
			bbos[i]=bba[i-1];
//		if(write()==op.write)
//			so.reqwrite();
		write(); // return ignored because bbos will be set to null when write is finished
	}
	private ByteBuffer make_header(final int size_of_data_to_send,final boolean text_mode){
		// rfc6455#section-5.2
		// Base Framing Protocol
		int nhdr;
		final byte[]hdr=new byte[10];
		hdr[0]=(byte)((text_mode?1:2)|128);
		if(size_of_data_to_send<=125){
			hdr[1]=(byte)size_of_data_to_send;
			nhdr=2;
		}else if(size_of_data_to_send<=65535){
			hdr[1]=126;
			hdr[2]=(byte)((size_of_data_to_send>>8)&255);
			hdr[3]=(byte)( size_of_data_to_send    &255);
			nhdr=4;
		}else{
			hdr[1]=127;
//			hdr[2]=(byte)((ndata>>56)&255);
//			hdr[3]=(byte)((ndata>>48)&255);
//			hdr[4]=(byte)((ndata>>40)&255);
//			hdr[5]=(byte)((ndata>>32)&255);
			hdr[6]=(byte)((size_of_data_to_send>>24)&255);
			hdr[7]=(byte)((size_of_data_to_send>>16)&255);
			hdr[8]=(byte)((size_of_data_to_send>> 8)&255);
			hdr[9]=(byte)( size_of_data_to_send     &255);
			nhdr=10;
		}
		return ByteBuffer.wrap(hdr,0,nhdr);
	}
	final protected boolean issending(){return bbos!=null;}
}