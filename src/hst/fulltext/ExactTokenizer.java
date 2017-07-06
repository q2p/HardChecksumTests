package hst.fulltext;

import java.nio.CharBuffer;
import java.util.*;

public final class ExactTokenizer implements Tokenizer {
	public static final ExactTokenizer INSTANCE = new ExactTokenizer();

	public static final char[][] captured = {
		"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray(),
		"абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".toCharArray(),
		"0123456789".toCharArray()
	};
	public static final char[] separators = " _+=-.,/[]{};:'\"<>\\|?!@#$%^&*()`~\n\r\t".toCharArray();

	private static final boolean lowerify = true;

	static {
		for(char[] array : captured)
			Arrays.sort(array);

		Arrays.sort(separators);
	}

	public final void tokenize(final String document, final LinkedHashMap<String, LinkedList<Integer>> out) {
		final LinkedList<String> temp = new LinkedList<String>();
		CharBuffer cb =  CharBuffer.wrap(document);
		int type = -1;
		boolean haveContent = false;
		StringBuilder sb = new StringBuilder();

		while(cb.hasRemaining()) {
			char c = cb.get();

			if(lowerify && Character.isUpperCase(c))
				c = Character.toLowerCase(c);

			int ntype = -2;

			for(byte i = 2; i != -1; i--) {
				if(Arrays.binarySearch(captured[i], c) >= 0) {
					ntype = i;
					break;
				}
			}
			if(ntype == -2) {
				if(Arrays.binarySearch(separators, c) >= 0 || Character.isWhitespace(c))
					ntype = -1;
				else if(Character.isDigit(c))
					ntype = 3;
				else if(Character.isSurrogate(c))
					ntype = 4;
				else if(Character.isLetter(c))
					ntype = 5;
				else
					ntype = -1;
			}

			if(ntype == -1) {
				if(haveContent) {
					temp.add(sb.toString());
					sb.setLength(0);
					haveContent = false;
				}
				type = -1;
				temp.add(""+c);
			} else {
				if(type != ntype && haveContent) {
					temp.add(sb.toString());
					sb.setLength(0);
				}
				type = ntype;
				sb.append(c);
				haveContent = true;
			}
		}

		while(!temp.isEmpty()) {
			LinkedList<Integer> bb = new LinkedList<Integer>();
			bb.addLast(0);
			bb = out.putIfAbsent(temp.removeFirst(), bb);
			if(bb != null)
				bb.addLast(0);
		}
	}
}