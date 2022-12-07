package db.test;

import db.Db;
import db.DbTransaction;

public abstract class TestCase implements Runnable {
	private final boolean use_current_transaction;

	public TestCase() {
		this(false);
	}

	public TestCase(final boolean use_current_transaction) {
		this.use_current_transaction = use_current_transaction;
	}

	public final void run() {
		final boolean rst = isResetDatabase();

		if (rst) {
			try {
				Db.currentTransaction().commit(); // note Db.reset hangs mysql on drop "table session" otherwise.
				// possibly because session was updated but not committed
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
			Db.instance().reset();
		}

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

	public String getTestName() {
		return getClass().getName();
	}

	private void doTest(final boolean cacheon) {
		final DbTransaction tn;
		if (use_current_transaction) {
			tn = Db.currentTransaction();
		} else {
			tn = Db.initCurrentTransaction();
		}
		final String cachests = cacheon ? " on" : "off";
		tn.cache_enabled = cacheon;
		try {
			final long t0 = System.currentTimeMillis();
			doRun();
			tn.commit();
			final long t1 = System.currentTimeMillis();
			final long dt = t1 - t0;
			final long dt_s = dt / 1000;
			System.out.println(getTestName() + " [cache " + cachests + "]: passed (" + dt_s + "s)");
		} catch (Throwable t1) {
			if (!use_current_transaction) {
				tn.rollback();
			}
			System.out.println(getClass().getName() + " [cache " + cachests + "]: failed");
			throw new RuntimeException(t1);
		} finally {
			if (!use_current_transaction) {
				Db.deinitCurrentTransaction();
			}
		}
	}

	public abstract void doRun() throws Throwable;
}
