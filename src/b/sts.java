package b;

/** Interface to an object that receives status updates. */
public interface sts {
	void sts_set(String s) throws Throwable;

	void sts_flush() throws Throwable;
}
