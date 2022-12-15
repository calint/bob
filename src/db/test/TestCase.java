package db.test;

import java.io.PrintStream;

import db.Db;
import db.DbTransaction;

public abstract class TestCase implements Runnable {
	private final boolean use_current_transaction;
	public PrintStream out = System.out;
	public int number_of_runs = 1;
	public boolean data_points_output;
	public boolean run_with_cache = true;
	public boolean run_without_cache = false;

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
			Db.reset();
		}

		if (run_with_cache)
			doTest(true);

		if (rst)
			Db.reset();

		if (run_without_cache)
			doTest(false);
	}

	/** @return true to reset database before tests */
	protected boolean isResetDatabase() {
		return false;
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
		if (data_points_output) {
			out.println("run\tms");
		} else {
			out.println(getClass().getName() + ":");
		}
		out.flush();
		try {
			final long t0 = System.currentTimeMillis();
			for (int i = 0; i < number_of_runs; i++) {
				if (data_points_output) {
					out.print(i + 1);
					out.print('\t');
					out.flush();
				} else {
					out.print(getClass().getName() + ": run " + (i + 1) + " of " + number_of_runs);
					out.flush();
				}
				final long t2 = System.currentTimeMillis();
				doRun();
				final long t3 = System.currentTimeMillis();
				if (data_points_output) {
					out.println(t3 - t2);
				} else {
					out.println(" " + (t3 - t2) + " ms");
				}
				tn.commit();
			}
			final long t1 = System.currentTimeMillis();
			final long dt = t1 - t0;
			if (!data_points_output)
				out.println(getTestName() + " [cache " + cachests + "]: passed (" + dt + " ms)");
//			out.flush();
		} catch (Throwable t1) {
			if (!use_current_transaction) {
				tn.rollback();
			}
			out.println(getClass().getName() + " [cache " + cachests + "]: failed");
//			out.flush();
			throw new RuntimeException(t1);
		} finally {
			if (!use_current_transaction) {
				Db.deinitCurrentTransaction();
			}
		}
	}

	public abstract void doRun() throws Throwable;
}
