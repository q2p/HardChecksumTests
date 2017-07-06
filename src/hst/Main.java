package hst;

import hst.fulltext.Documentizer;
import hst.singlesearches.trees.AlignedNameTree;
import hst.singlesearches.trees.AlignedNotPerfectTree;
import hst.singlesearches.trees.NameTree;
import hst.singlesearches.trees.NotPerfectTree;

import java.io.File;

public final class Main {
	private static final File file = new File("test.dat");

//	private static final byte eqKey = 512/8;
	private static final byte eqKey = 1;
	private static final int eqFiles = 20_000;

	private static final int message = 10000;

	private static final int stringLength = 80;

	public static void main(final String[] args) throws Exception {
//		System.out.println("NP128");
//		Tester.testNonPerfect(new NotPerfectTree(file,
//			eqKey, 128), eqFiles, eqKey, message);

//		System.out.println("A4K");
//		Tester.testNonPerfect(new AlignedNotPerfectTree(file,
//			4096, eqKey), eqFiles, eqKey, message);

//		System.out.println("A4K128");
//		Tester.testNonPerfect(new AlignedNotPerfectTree(file,
//			4096, eqKey, 128), eqFiles, eqKey, message);

//		Tester.testNonPerfect(new AlignedNotPerfectTree(file,
//			256, 512/8), 256*1024, 512/8, message);
//
//		System.out.println("N 128");
//		Tester.testNames(new NameTree(file,
//			stringLength, 128), eqFiles, stringLength, message);
//
//		System.out.println("N 64");
//		Tester.testNames(new NameTree(file,
//			stringLength, 64), eqFiles, stringLength, message);
//
//		System.out.println("N 64*3");
//		Tester.testNames(new NameTree(file,
//			stringLength, 64*3), eqFiles, stringLength, message);
//
//		System.out.println("N 256");
//		Tester.testNames(new NameTree(file,
//			stringLength, 256), eqFiles, stringLength, message);
//
//		System.out.println("AN 2K");
//		Tester.testNames(new AlignedNameTree(file,
//			1024*2, stringLength), eqFiles, stringLength, message);
//
//		System.out.println("AN 4K");
//		Tester.testNames(new AlignedNameTree(file,
//			4096, stringLength), eqFiles, stringLength, message);
//
//		Documentizer.test();
	}
}
