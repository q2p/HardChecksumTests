package hst.help;

public final class Profiler {
	private long[] starts;
	private long[] stored;

	public Profiler(final int bucketsAmount) {
		assert bucketsAmount >= 1;
		starts = new long[bucketsAmount];
		stored = new long[bucketsAmount];
	}

	public final void start(final int bucket) {
		starts[bucket] = System.nanoTime();
		stored[bucket] = 0;
	}

	public final void stop(final int bucket) {
		stored[bucket] += System.nanoTime() - starts[bucket];
	}

	public final void resetAll() {
		for(int i = stored.length-1; i != -1; i--)
			stored[i] = 0;
	}

	public final void reset(final int bucket) {
		stored[bucket] = 0;
	}
	
	public final Profiler pull(final int bucket, final String text) {
		long total = stored[bucket];
		int nano  = (int)((total) % 1_000);
		int micro = (int)((total / (1_000)) % 1000);
		int mili  = (int)((total / (1_000_000)) % 1000);
		int sec   = (int)((total / (1_000_000_000L)));
		StringBuilder sb = new StringBuilder(text.length()+2+4+4+4+3+2);
		sb.append(text);
		sb.append(": ");
		pad(sb, 3, sec);
		sb.append(".");
		pad(sb, 3, mili);
		sb.append(",");
		pad(sb, 3, micro);
		sb.append(",");
		pad(sb, 3, nano);
		sb.append(" s");

		System.out.println(sb.toString());
		return this;
	}

	private void pad(final StringBuilder sb, final int length, final int num) {
		for(int i = length - (""+num).length(); i > 0; i--)
			sb.append('0');

		sb.append(num);
	}
}