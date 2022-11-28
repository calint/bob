package b;final class jvmsdh extends Thread{public void run(){
	thdwatch._stop=true;
	if(b.bapp!=null){
		try{b.bapp.shutdown();}catch(final Throwable e){e.printStackTrace();}
	}
	if(!b.sessions_save_at_shutdown)return;
	try{session.all_save();}catch(final Throwable e){e.printStackTrace();}
}}