package hst.help;

public final class Timer {
	private int milisecounds;
	private long last;
	private String message;
	private int total;

	public Timer() {
	}

	public final void start(final String message, final int total, final int milisecounds) {
		last = System.currentTimeMillis();
		this.message = message;
		this.total = total;
		this.milisecounds = milisecounds;
	}

	public final void check(final int counter) {
		final long nt = System.currentTimeMillis();
		if(nt - last < milisecounds)
			return;

		last += ((nt - last)/milisecounds)*milisecounds;

		StringBuilder sb = new StringBuilder(message);
		sb.append(": ");
		pad(sb, (""+total).length(), counter);
		sb.append('/');
		sb.append(total);
		sb.append(' ');
		perc(sb, counter);

		System.out.println(sb.toString());
	}

	private void perc(final StringBuilder sb, int counter) {
		int ret = (int)((float)counter*1_00_00f/total);

		for(int i = 3 - (""+ret/100).length(); i > 0; i--)
			sb.append('0');

		sb.append(ret/100);

		sb.append('.');

		for(int i = 3 - (""+ret%100).length(); i > 0; i--)
			sb.append('0');

		sb.append(ret%100);

		sb.append('%');
	}

	private void pad(final StringBuilder sb, final int length, final int num) {
		for(int i = length - (""+num).length(); i > 0; i--)
			sb.append('0');

		sb.append(num);
	}
}