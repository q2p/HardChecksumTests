package hst.singlesearches.trees;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

public abstract class NameStoreTree {
	public final int maxNameLength;

	protected NameStoreTree(final int maxNameLength) {
		assert maxNameLength > 0 && maxNameLength <= 0xFF;
		this.maxNameLength = maxNameLength;
	}

	public abstract int get(byte[] rawArray);

	public abstract void set(byte[] rawArray, int value);

	public abstract void remove(byte[] rawArray);

	public int get(final String string) {
		return get(encode(string));
	}

	public void set(final String string, final int value) {
		set(encode(string), value);
	}

	public void remove(final String string) {
		remove(encode(string));
	}

	public byte[] encode(final CharSequence string) {
		ByteBuffer bb = ByteBuffer.allocate(1+ maxNameLength);
		bb.position(1);

		int length = string.length();

		assert length > 0 && length <= maxNameLength;

		final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();

		encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		encoder.onMalformedInput(CodingErrorAction.REPLACE);

		encoder.encode(CharBuffer.wrap(string), bb, true);
		encoder.flush(bb);
		bb.put(0, (byte)(bb.position()-1));

		return bb.array();
	}
}