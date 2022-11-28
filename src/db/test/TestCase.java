package db.test;

import db.Db;
import db.DbTransaction;

public abstract class TestCase implements Runnable {
	public final void run() {
		final boolean rst = isResetDatabase();

		if (rst)
			Db.instance().reset();

		if (isRunWithCache())
			doTest(true);

		if (rst)
			Db.instance().reset();

		if (isRunWithoutCache())
			doTest(false);
	}

	/** @return true to reset database before tests */
	protected boolean isResetDatabase() {
		return false;
	}

	/** @return true to run test with cache on */
	protected boolean isRunWithCache() {
		return true;
	}

	/** @return true to run test with cache off */
	protected boolean isRunWithoutCache() {
		return true;
	}

	protected String getTestName() {
		return getClass().getName();
	}

	private void doTest(final boolean cacheon) {
		final DbTransaction tn;
		final String cachests = cacheon ? " on" : "off";
		try {
			tn = Db.initCurrentTransaction();
			tn.cache_enabled = cacheon;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		try {
			final long t0 = System.currentTimeMillis();
			doRun();
			tn.finishTransaction();
			final long t1 = System.currentTimeMillis();
			final long dt = t1 - t0;
			final long dt_s = dt / 1000;
			System.out.println(getTestName() + " [cache " + cachests + "]: passed (" + dt_s + "s)");
		} catch (Throwable t1) {
			try {
				tn.rollback();
			} catch (Throwable t2) {
				t2.printStackTrace();
			}
			System.out.println(getClass().getName() + " [cache " + cachests + "]: failed");
			throw new RuntimeException(t1);
		} finally {
			Db.deinitCurrentTransaction();
		}
	}

	public abstract void doRun() throws Throwable;
}