package hst.help;

public final class Watch {
	private static Profiler localProfiler = new Profiler(1);

	public static void till(final String message, final Runnable r) {
		localProfiler.start(0);
		r.run();
		localProfiler.stop(0);
		localProfiler.pull(0, message);
	}
	public static <T> T resolve(final String message, Running<T> r) {
		localProfiler.start(0);
		final T ret = r.run();
		localProfiler.stop(0);
		localProfiler.pull(0, message);
		return ret;
	}

	public interface Running<T> {
		T run();
	}
}
