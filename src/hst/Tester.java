package hst;

import hst.help.Timer;
import hst.help.Watch;
import hst.singlesearches.NotPerfectHashStore;
import hst.singlesearches.trees.NameStoreTree;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Random;

public final class Tester {
	public static void testNonPerfect(final NotPerfectHashStore instance, final int entries, final int bytesPerSum, final int messageInterval) {
		final byte[][] hashes = Watch.resolve("Генерация", () -> generateHashes(entries, bytesPerSum));

		Watch.till("Заполнение", () -> fill(instance, hashes, messageInterval));
		Watch.till("Проверка", () -> check(instance, hashes, messageInterval));
		System.out.println();
	}
	public static void testNames(final NameStoreTree instance, final int entries, final int maxNameLength, final int messageInterval) {
		final String[] hashes = Watch.resolve("Генерация", () -> generateNames(entries, maxNameLength));

		Watch.till("Заполнение", () -> fill(instance, hashes, messageInterval));
		Watch.till("Проверка", () -> check(instance, hashes, messageInterval));
		System.out.println();
	}

	private static void fill(final NotPerfectHashStore instance, final byte[][] hashes, final int messageInterval) {
		int i = 0;
		Timer timer = new Timer();
		timer.start("Заполнение", hashes.length, messageInterval);
		for(final byte[] hash : hashes) {
			timer.check(i);
			instance.add(hash, i++);
		}
	}

	private static void fill(final NameStoreTree instance, final String[] names, int messageInterval) {
		int i = 0;
		Timer timer = new Timer();
		timer.start("Заполнение", names.length, messageInterval);
		for(final String name : names) {
			timer.check(i);
			instance.set(name, i++);
		}
	}

	private static void check(final NotPerfectHashStore instance, final byte[][] hashes, final int messageInterval) {
		Timer timer = new Timer();
		timer.start("Проверка", hashes.length, messageInterval);
		mainLoop:
		for(int i = 0; i != hashes.length; i++) {
			timer.check(i);

			int[] arr = instance.get(hashes[i]);

			if(arr != null) {
				IntBuffer ib = IntBuffer.wrap(arr);
				for(int j = arr.length; j != 0; j--) {
					if(ib.get() == i)
						continue mainLoop;
				}
			}

			System.out.println("Не удалось найти все элементы.");
			return;
		}
	}

	private static void check(final NameStoreTree instance, final String[] names, int messageInterval) {
		Timer timer = new Timer();
		timer.start("Проверка", names.length, messageInterval);
		for(int i = 0; i != names.length; i++) {
			timer.check(i);
			if(instance.get(names[i]) != i) {
				System.out.println("Не удалось найти все элементы.");
				return;
			}
		}
	}

	private static byte[][] generateHashes(final int amount, final int bytesPerSum) {
		final Random r = new Random(32);

		final byte[][] ret = new byte[amount][bytesPerSum];

		for(int i = 0; i != amount; i++)
			r.nextBytes(ret[i]);

		return ret;
	}

	private static final char[] valid = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+_-!@#$%^&*()<>,./\\'".toCharArray();
	private static String[] generateNames(final int amount, final int maxNameLength) {
		final Random r = new Random(32);

		final String[] ret = new String[amount];

		final StringBuilder sb = new StringBuilder(maxNameLength);

		final HashSet<String> comparator = new HashSet<String>();

		String test;

		for(int i = 0; i != amount; i++) {
			do {
				final int length = r.nextInt(maxNameLength)+1;

				for(int j = length; j != 0; j--)
					sb.append(valid[r.nextInt(valid.length)]);

				test = sb.toString();

				sb.delete(0, maxNameLength);
			} while(comparator.contains(test));

			ret[i] = test;

			comparator.add(test);
		}

		return ret;
	}
}
