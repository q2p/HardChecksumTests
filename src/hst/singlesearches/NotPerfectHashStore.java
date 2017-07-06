package hst.singlesearches;

public interface NotPerfectHashStore {
	int[] get(byte[] key);

	void add(byte[] key, int value);

	int[] getOrAddIfNotExists(final byte[] key, int value);

	void remove(byte[] key, int value);
}