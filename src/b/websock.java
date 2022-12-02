package b;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.util.Map;
public class websock implements sock{
	private ByteBuffer bbi;
	private static enum state{initiation,closed,handshake,read_next_frame,read_continue}
	private state st=state.initiation;
	private final byte[]maskkey=new byte[4];
	private int payload_remaining;
	private ByteBuffer[]response_bba;
	private boolean is_first_packet;
	private int maskc;
	private boolean masked;
	private ByteBuffer request_bb;
	private SocketChannel socket_channel;
	/** @param bbspil byte buffer might have more data to be read */
	final public op sockinit(final Map<String,String>hdrs,final SocketChannel sc,final ByteBuffer bbspil)throws Throwable{
		socket_channel=sc;
		bbi=bbspil;
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
		System.out.println("@@@@@ 2:   "+new String(bbo.array(),bbo.position(),bbo.remaining()));
		while(bbo.hasRemaining()&&sc.write(bbo)!=0);
		if(bbo.hasRemaining())
			throw new RuntimeException("packetnotfullysent");
//		bbi.position(bbi.limit());
		onopened();
		st=state.read_next_frame;
		return op.read; // response sent, wait for packet (assumes client hasn't sent anything yet
	}
	protected void onopened()throws Throwable{}
	final public op read()throws Throwable{
		if(!bbi.hasRemaining()){
			bbi.clear();
			final int n=socket_channel.read(bbi);
			thdwatch.input+=n;

			if(n==0)return op.read;//? infloop?
			if(n==-1){
				st=state.closed;
				return op.close; // onclosed called when request is closed
			}
			bbi.flip();
		}
		while(true) {
			switch(st){default:throw new RuntimeException();
			case read_next_frame: // ? assuming the header is buffered. breaking up into states for header would handle the input buffer of 1 B
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
					return op.close; // onclose called when request is closed
				}
				final int b1=(int)bbi.get();
				masked=(b1&128)==128;
				payload_remaining=b1&127;
				if(payload_remaining==126){
					final int by2=(((int)bbi.get()&0xff)<<8);
					final int by1= ((int)bbi.get()&0xff);
					payload_remaining=by2|by1;
				}else if(payload_remaining==127){
					bbi.get();// skip the bytes that encode a length >4G
					bbi.get();
					bbi.get();
					bbi.get();
					final int by4=(((int)bbi.get()&0xff)<<24);
					final int by3=(((int)bbi.get()&0xff)<<16);
					final int by2=(((int)bbi.get()&0xff)<<8);
					final int by1= ((int)bbi.get()&0xff);
					payload_remaining=by4|by3|by2|by1;
				}
				bbi.get(maskkey);
				is_first_packet=true;
				maskc=0;
				st=state.read_continue;
				// fall through
			case read_continue:
				// unmask
				final byte[]bbia=bbi.array();
				final int pos=bbi.position();
				final int limit=bbi.remaining()>payload_remaining?pos+payload_remaining:bbi.limit();
				if(masked&&maskkey[0]==0&&maskkey[1]==0&&maskkey[2]==0&&maskkey[3]==0){
					throw new RuntimeException();
				}
				for(int i=pos;i<limit;i++){
					final byte b=(byte)(bbia[i]^maskkey[maskc]);
					bbia[i]=b;
					maskc++;
					if(maskc==maskkey.length) {
						maskc=0;
					}
				}
				
				final int read_length=limit-pos;
				payload_remaining-=read_length;
				if(payload_remaining==0){ // data has been fully read
					st=state.read_next_frame;
				}
				final ByteBuffer bbii=ByteBuffer.wrap(bbia,pos,read_length);// bbi position is start of data, limit is the data unmasked
				onpayload(bbii);
				is_first_packet=false;
				bbi.position(limit);
				if(bbi.remaining()!=0){
					// client has sent multiple packets without waiting for response.
					// this is not a supported use case.
					// may work if write() completes the send.
					if(response_bba!=null)
						throw new RuntimeException("chained request with send() overrun");
					continue;
				}
				return response_bba==null?op.read:op.write; // onpayload->onmessage might have done a send that is not complete
			}
		}
	}
	final private void onpayload(ByteBuffer bb)throws Throwable{
		final boolean is_last_packet=payload_remaining==0;
		if(is_first_packet&&!is_last_packet){
			request_bb=ByteBuffer.allocate(bb.remaining()+payload_remaining);
			request_bb.put(bb);
			return;
		}
		if(!is_first_packet&&!is_last_packet){
			request_bb.put(bb);
			return;
		}
		if(!is_first_packet&&is_last_packet){
			request_bb.put(bb);
			request_bb.flip();
		}
		if(is_first_packet&&is_last_packet){
			request_bb=bb;
		}
		onmessage(request_bb);
		request_bb=null;
	}

	@Override public void onconnectionlost()throws Throwable{
		onclosed();
	}

	/** Called by the request or by send(...) */
	final public op write()throws Throwable{
		final long c=socket_channel.write(response_bba);
		thdwatch.output+=c;
		for(ByteBuffer b:response_bba){ // check if the write is complete.
			if(b.hasRemaining()){
				return op.write; // will trigger a new write when called from request
			}
		}
		response_bba=null; // will trigger a read request when called from send(...). return read to thdreq
		return op.read;
	}

	/** Called when the web socket has been closed. */
	protected void onclosed()throws Throwable{}
	
	/** Called when a message has been decoded. ByteBuffer position is at start of data and limit marks the end of data.
	 *  Note that it is assumed that client does not send a new message prior to receiving a reply. */
	protected void onmessage(ByteBuffer bb)throws Throwable{}
	
	final public void send(String s)throws Throwable{
		send(new ByteBuffer[]{ByteBuffer.wrap(s.getBytes())},true);
	}
	
	final public void send(ByteBuffer bb,final boolean textmode)throws Throwable{
		if(response_bba!=null)throw new Error("overwrite");//?
		// rfc6455#section-5.2
		// Base Framing Protocol
		final int ndata=bb.remaining();
		response_bba=new ByteBuffer[]{make_header(ndata,textmode),bb};
		write(); // return ignored because bbos will be set to null when write is finished
	}
	final public void send(final ByteBuffer[]bba,final boolean textmode)throws Throwable{
		if(response_bba!=null)
			throw new RuntimeException("send did not complete before a new send was called"); // ? is the buffer size enough for most use cases?
		int ndata=0;
		for(final ByteBuffer b:bba)
			ndata+=b.remaining();
		response_bba=new ByteBuffer[bba.length+1];
		response_bba[0]=make_header(ndata,textmode);
		for(int i=1;i<response_bba.length;i++)
			response_bba[i]=bba[i-1];
		write(); // return ignored because response_bba will be set to null when write is finished
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
//			hdr[2]=(byte)((ndata>>56)&255); // ignore size bigger than 4G
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
	
	@Override
	public String toString() {
		return new String(bbi.array(),bbi.position(),bbi.remaining());
	}
}