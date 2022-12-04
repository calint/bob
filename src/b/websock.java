package b;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.Map;

/**
 * Use case is request response chain. Full duplex not supported on one thread.
 */
public class websock implements sock{
	private static enum state{
		handshake,parse_next_frame,parse_data,closed
	}
	private SocketChannel socket_channel;
	private ByteBuffer bb;
	private state st=state.handshake;
	private int payload_remaining;
	private ByteBuffer request_bb;
	private ByteBuffer[] send_bba;
	private final LinkedList<ByteBuffer[]> send_que=new LinkedList<>();
	private boolean is_first_packet;
	private int mask_i;
	private boolean masked;
	private final byte[] maskkey=new byte[4];
	/** @param bb byte buffer might have more data to be read */
	final public op sock_init(final Map<String,String> headers,final SocketChannel sc,final ByteBuffer bb) throws Throwable{
		socket_channel=sc;
		this.bb=bb;
		// rfc6455#section-1.3
		// Opening Handshake
		final String key=headers.get("sec-websocket-key");
		final String s=key+"258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		final byte[] sha1ed=MessageDigest.getInstance("SHA-1").digest(s.getBytes());
		final String replkey=base64.encodeToString(sha1ed,true);
		final ByteBuffer bbo=ByteBuffer.allocate(b.K>>2);
//		final String prot=hdrs.get("sec-webSocket-protocol");
		bbo.put("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: ".getBytes());
		bbo.put(replkey.getBytes());
//		bbo.put("\r\nSec-WebSocket-Protocol: chat".getBytes());
		// ? add session cookie
		bbo.put("\r\n\r\n".getBytes());
		bbo.flip();
		while(bbo.hasRemaining()&&sc.write(bbo)!=0);
		if(bbo.hasRemaining())
			throw new RuntimeException("packetnotfullysent");
		on_opened(headers);
		st=state.parse_next_frame;
		return op.read; // response sent, wait for packet (assumes client hasn't sent begun sending
						// anything yet)
	}
	protected void on_opened(Map<String,String> headers) throws Throwable{
	}
	final public op sock_read() throws Throwable{
		bb.clear();
		final int n=socket_channel.read(bb);
		System.out.println("websock "+Integer.toHexString(hashCode())+": sock_read: "+n+" bytes");
		thdwatch.input+=n;
		if(n==0)
			return op.read;// ? infloop?
		if(n==-1){
			st=state.closed;
			return op.close; // on_connection_lost called when request is closed
		}
		bb.flip();
		while(true){
			switch(st){
			default:
				throw new RuntimeException();
			case parse_next_frame: // ? assuming the header is buffered. breaking up into states for header would
									// handle the input buffer of 1 B
				// rfc6455#section-5.2
				// Base Framing Protocol
				final int b0=(int)bb.get();
				final boolean fin=(b0&128)==128;
				if(fin)
					;// to remove warning of unused variable
				final int resv=(b0>>4)&7;
				if(resv!=0)
					throw new Error("reserved bits are not 0");
				final int opcode=b0&0xf;
				if(opcode==8){// rfc6455#section-5.5.1
					st=state.closed;
					return op.close; // onclose called when request is closed
				}
				// todo handle the other opcodes
				// https://www.rfc-editor.org/rfc/rfc6455#section-5.2

				// parse header
				final int b1=(int)bb.get();
				masked=(b1&128)==128;
				payload_remaining=b1&127;
				if(payload_remaining==126){
					final int by2=(((int)bb.get()&0xff)<<8);
					final int by1=((int)bb.get()&0xff);
					payload_remaining=by2|by1;
				}else if(payload_remaining==127){
					bb.get();// skip the bytes that encode a length >4G
					bb.get();
					bb.get();
					bb.get();
					final int by4=(((int)bb.get()&0xff)<<24);
					final int by3=(((int)bb.get()&0xff)<<16);
					final int by2=(((int)bb.get()&0xff)<<8);
					final int by1=((int)bb.get()&0xff);
					payload_remaining=by4|by3|by2|by1;
				}
				bb.get(maskkey);
				is_first_packet=true;
				mask_i=0;
				st=state.parse_data;
				// fall through
			case parse_data:
				final byte[] bbia=bb.array();
				final int pos=bb.position();
				final int limit=bb.remaining()>payload_remaining?pos+payload_remaining:bb.limit();
				if(masked&&maskkey[0]==0&&maskkey[1]==0&&maskkey[2]==0&&maskkey[3]==0){
					throw new RuntimeException();
				}
				// unmask
				for(int i=pos;i<limit;i++){
					final byte b=(byte)(bbia[i]^maskkey[mask_i]);
					bbia[i]=b;
					mask_i++;
					if(mask_i==maskkey.length){
						mask_i=0;
					}
				}
				bb.position(limit); // sets to the current position

				final int read_length=limit-pos; // number of bytes read from the buffer
				payload_remaining-=read_length;
				if(payload_remaining==0){ // data has been fully read
					st=state.parse_next_frame;
				}
				final ByteBuffer bbii=ByteBuffer.wrap(bbia,pos,read_length);// bbia position is start of data, limit is
																			// the data unmasked
				onpayload(bbii);
				is_first_packet=false;
				synchronized(send_que){
					if(bb.remaining()!=0){
						// more messages
						continue;
					}
					if(send_bba!=null){
						// onpayload->on_message might have done send that is incomplete. request write
						// from thdreq which will start calling sock_write until send is done.
						// When sock_write is done sending it will request a read.
						return op.write;
					}
					return op.read; // done. request read.
				}
			}
		}
	}
	final private void onpayload(ByteBuffer bb) throws Throwable{
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
		on_message(request_bb);
		request_bb=null;
	}
	/** Called by the request or by send(...) */
	final public op sock_write() throws Throwable{
		synchronized(send_que){
			while(send_bba!=null){
				final long n=socket_channel.write(send_bba);
				System.out.println("websock "+Integer.toHexString(hashCode())+": sock_write: "+n+" bytes");
				thdwatch.output+=n;
				for(ByteBuffer b:send_bba){ // check if the write is complete.
					if(b.hasRemaining()){
						// if called from thdreq then request more writes otherwise return value is
						// ignored
						return op.write;
					}
				}
				send_bba=send_que.pollFirst();
			}
			// if called from thdreq then request read otherwise return value is ignored
			return op.read;
		}

	}

	@Override public void sock_on_closed() throws Throwable{
		on_closed();
	}

	/** Called when the web socket has been closed. */
	protected void on_closed() throws Throwable{
	}

	/**
	 * Called when a message has been decoded. ByteBuffer position is at start of data and limit marks the end of data.
	 */
	protected void on_message(ByteBuffer bb) throws Throwable{
	}

	final public void send(String s) throws Throwable{
		send(new ByteBuffer[]{ByteBuffer.wrap(s.getBytes())},true);
	}

	final public void send(ByteBuffer bb,final boolean textmode) throws Throwable{
		send(new ByteBuffer[]{bb},textmode);
//		if(response_bba!=null)throw new Error("overwrite");//?
//		// rfc6455#section-5.2
//		// Base Framing Protocol
//		final int ndata=bb.remaining();
//		response_bba=new ByteBuffer[]{make_header(ndata,textmode),bb};
//		sock_write(); // return ignored because bbos will be set to null when write is finished
	}
	final public void send(final ByteBuffer[] bba,final boolean textmode) throws Throwable{
		int nbytes_to_send=0;
		for(final ByteBuffer b:bba)
			nbytes_to_send+=b.remaining();

		final ByteBuffer[] bbout=new ByteBuffer[bba.length+1];
		bbout[0]=make_header(nbytes_to_send,textmode);
		for(int i=1;i<bbout.length;i++)
			bbout[i]=bba[i-1];

		synchronized(send_que){
			if(send_bba!=null){
				send_que.add(bbout);
				return;
			}else{
				send_bba=bbout;
			}
		}
		sock_write(); // return ignored because response_bba will be set to null when write is
						// finished
	}

	private ByteBuffer make_header(final int size_of_data_to_send,final boolean text_mode){
		// rfc6455#section-5.2
		// Base Framing Protocol
		int nhdr;
		final byte[] hdr=new byte[10];
		hdr[0]=(byte)((text_mode?1:2)|128);
		if(size_of_data_to_send<=125){
			hdr[1]=(byte)size_of_data_to_send;
			nhdr=2;
		}else if(size_of_data_to_send<=65535){
			hdr[1]=126;
			hdr[2]=(byte)((size_of_data_to_send>>8)&255);
			hdr[3]=(byte)(size_of_data_to_send&255);
			nhdr=4;
		}else{
			hdr[1]=127;
//			hdr[2]=(byte)((ndata>>56)&255); // ignore size bigger than 4G
//			hdr[3]=(byte)((ndata>>48)&255);
//			hdr[4]=(byte)((ndata>>40)&255);
//			hdr[5]=(byte)((ndata>>32)&255);
			hdr[6]=(byte)((size_of_data_to_send>>24)&255);
			hdr[7]=(byte)((size_of_data_to_send>>16)&255);
			hdr[8]=(byte)((size_of_data_to_send>>8)&255);
			hdr[9]=(byte)(size_of_data_to_send&255);
			nhdr=10;
		}
		return ByteBuffer.wrap(hdr,0,nhdr);
	}

	@Override public String toString(){
		return new String(bb.array(),bb.position(),bb.remaining());
	}
}
