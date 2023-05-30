package b;

import db.Db;

final class jvmsdh extends Thread{
	@Override public void run(){
		thdwatch._stop=true;
		if(b.bapps!=null){
			for(int i=0;i<b.bapps.length;i++){
				try{
					b.log("shutdown: "+b.bapps[i].getClass().getName());
					b.bapps[i].shutdown();
				}catch(final Throwable e){
					e.printStackTrace();
				}
			}
		}
		Db.shutdown();
	}
}
