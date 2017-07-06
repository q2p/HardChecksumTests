package hst.fulltext;

import hst.help.Coder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Documentizer {
	private static final String pPath = "E:/@MyFolder/p/";
	private static final String fics = pPath+"Fanfictions/";
	private static final String ideas = pPath+"other/ideas/dat.js";
	private static final String ideas2 = pPath+"mix/ids.txt";

	private static final String tags = pPath+"/other/tagsStuff/";
	private static final String tagsr = tags+"r34_tags.txt";
	private static final String tagsd = tags+"derpibooru_tags.txt";
	private static final String tagse = tags+"e621_tags.txt";

	private static LinkedHashMap<String, LinkedList<Integer>> store = new LinkedHashMap<String, LinkedList<Integer>>();

	public static void test() {
//		loadFics();
//		loadIdeas();
		loadTags();
		flush();
	}

	private static void flush() {
		System.out.println();
		System.out.println("Flush");
		System.out.println(store.size());
		System.out.println();

		LinkedList<Chunk> is = new LinkedList<Chunk>();

		for(final Entry<String, LinkedList<Integer>> entry : store.entrySet())
			is.addLast(new Chunk(entry));

		Collections.sort(is, new MyComp());

		try(final FileOutputStream fos = new FileOutputStream("o.txt")) {
			while(!is.isEmpty())
				fos.write(is.removeFirst().toString().getBytes());
			fos.flush();
		} catch(final Exception ignore) {}

		/*// TODO: new Deflater.
		try(final GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream("o.txt"), 4096, false)) {
			while(!is.isEmpty())
				gzos.write(is.removeFirst().toString().getBytes());
			gzos.flush();
			gzos.finish();
		} catch(final Exception ignore) {}*/
	}

	private static final class MyComp implements Comparator<Chunk>{
		public final int compare(final Chunk e1, final Chunk e2) {
			if(e1.size == e2.size)
				return 0;
			else if(e1.size < e2.size)
				return 1;
			else
				return -1;
		}
	}

	private static final class Chunk {
		private String name;
		private int size;

		public Chunk(final Entry<String, LinkedList<Integer>> entry){
			name = entry.getKey();
			size = entry.getValue().size();
		}
		public final String toString(){
			return size + " : [" + name + "]\n";
		}
	}

	private static void loadTags() {
		System.out.println("rule34");
		String[] ss = splitTags(tagsr);
		for(int i = 1; i < ss.length; i += 3)
			ExactTokenizer.INSTANCE.tokenize(ss[i], store);

		System.out.println("dbooru");
		ss = splitTags(tagsd);
		for(int i = 0; i < ss.length; i += 4) {
			ExactTokenizer.INSTANCE.tokenize(ss[i], store);
//			ExactTokenizer.INSTANCE.tokenize(ss[i]+2, store);
		}

		System.out.println("e621");
		ss = splitTags(tagse);
		for(int i = 1; i < ss.length; i += 4)
			ExactTokenizer.INSTANCE.tokenize(ss[i], store);
	}

	private static String[] splitTags(final String path) {
		try(FileInputStream fis = new FileInputStream(new File(tagsr))) {
			int left = fis.available();
			int pos = 0;
			byte[] buffer = new byte[left];

			while(left != 0) {
				final int readed = fis.read(buffer, pos, left);
				left -= readed;
				pos += readed;
			}

			return Coder.decode(buffer).split(" ");
		} catch(final Exception ignore) {
			return null;
		}
	}

	private static void loadIdeas() {
		System.out.println("Ideas");

		readRaw(new File(ideas));
		readRaw(new File(ideas2));
	}

	private static void loadFics() {
		LinkedList<File> dirs = new LinkedList<File>();

		dirs.addLast(new File(fics));

		while(!dirs.isEmpty()) {
			File[] childs = dirs.removeFirst().listFiles();

			for(final File f : childs) {
				if(f.isDirectory())
					dirs.addLast(f);
				else if(f.isFile()) {
					System.out.println(f.getName());
					ExactTokenizer.INSTANCE.tokenize(f.getName(), store);
					readRaw(f);
				}
			}
		}
	}

	private static void readRaw(final File file) {
		try(FileInputStream fis = new FileInputStream(file)) {
			int left = fis.available();
			int pos = 0;
			byte[] buffer = new byte[left];

			while(left != 0) {
				final int readed = fis.read(buffer, pos, left);
				left -= readed;
				pos += readed;
			}

			ExactTokenizer.INSTANCE.tokenize(Coder.decode(buffer), store);
		} catch(final IOException ignore) {}
	}
}
