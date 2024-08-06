// reviewed: 2024-08-05
package b;

import db.Db;

/** Java hook for shutdown. */
final class jvmsdh extends Thread {
    @Override
    public void run() {
        thdwatch._stop = true;
        if (b.bapps != null) {
            for (int i = b.bapps.length - 1; i >= 0; i--) {
                try {
                    b.log("shutdown: " + b.bapps[i].getClass().getName());
                    b.bapps[i].shutdown();
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        Db.shutdown();
    }
}
