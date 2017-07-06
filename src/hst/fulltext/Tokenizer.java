package hst.fulltext;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public interface Tokenizer {
	void tokenize(String document, LinkedHashMap<String, LinkedList<Integer>> out);
}
