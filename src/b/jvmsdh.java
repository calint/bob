package b;
final class jvmsdh extends Thread{
	@Override public void run(){
		thdwatch._stop=true;
		if(b.bapp!=null){
			try{
				b.bapp.shutdown();
			}catch(final Throwable e){
				e.printStackTrace();
			}
		}
	}
}
