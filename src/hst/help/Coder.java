package hst.help;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class Coder {
	public static String decode(final byte[] array) {
		ByteBuffer bb = ByteBuffer.wrap(array);

		final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();

		decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		decoder.onMalformedInput(CodingErrorAction.REPLACE);

		try {
			return decoder.decode(bb).toString();
		} catch (final CharacterCodingException ignore) {
			return null;
		}
	}
}
