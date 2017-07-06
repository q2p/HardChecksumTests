package hst.singlesearches.trees;

import hst.singlesearches.NotPerfectHashStore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Stack;

/*
	корень - int
	последняя позиция записи - int
	пустая ветвь - int
	пустой список - int

	высота - int // ненужна

	// рандомное расположение

	[
		[
			кол-во ключей - int
			[
				ветвь - int
				ключ - len
				значение (список) - int
				ветвь - int
				...
				ключ - len
				значение (список) - int
				ветвь - int
			]
		]
		...
		[
			значение - int
			следующий - int
		]
	]
*/

public class NotPerfectTree implements NotPerfectHashStore {
	private static final byte IB = Integer.BYTES;

	private final int minKeySize;
	private final int minChildrenSize;
	private final int maxKeySize;
	private final int maxChildrenSize;

	private final int keyLength;

	private final RandomAccessFile raf;

	private final int nodeSize;
	private final byte[] root;

	private int rootPos;
	private int lastPos;
	private int emptyNodePos;
	private int emptyListPos;

	public NotPerfectTree(final File file, final int keyLength, final int maxKeys) {
		assert maxKeys % 2 == 0 && maxKeys >= 2;
		assert keyLength >= 1;
		assert file != null;

		maxKeySize = maxKeys;
		minKeySize = maxKeys / 2;

		maxChildrenSize = maxKeySize + 1;
		minChildrenSize = minKeySize + 1;

		this.keyLength = keyLength;
		nodeSize = IB+ maxKeySize*(IB+keyLength+IB)+IB;

		root = new byte[nodeSize];

		try {
			raf = new RandomAccessFile(file, "rw");

			// TODO: if(raf.length() > 4*IB) {
			if(false) {
				ByteBuffer bb = ByteBuffer.allocate(4*IB);

				raf.read(bb.array());

				rootPos = bb.getInt();
				lastPos = bb.getInt();
				emptyNodePos = bb.getInt();
				emptyListPos = bb.getInt();

				raf.seek(rootPos);
				raf.read(root);
			} else {
				rootPos = 4*IB;
				lastPos = 4*IB+nodeSize;
				emptyNodePos = -1;
				emptyListPos = -1;

				ByteBuffer bb = ByteBuffer.allocate(4*IB+nodeSize);

				bb.putInt(rootPos);
				bb.putInt(lastPos);
				bb.putInt(emptyNodePos);
				bb.putInt(emptyListPos);
				bb.putInt(0); // Ветви
				bb.putInt(-1); // Пустой указатель

				raf.write(bb.array());

				bb = ByteBuffer.wrap(root);
				bb.putInt(0);
				bb.putInt(-1);
			}
		} catch(final IOException e) {
			throw new AssertionError(e.getMessage(), e);
		}
	}

	public final int[] get(final byte[] key) {
		byte[] currentNode = new byte[nodeSize];
		System.arraycopy(root, 0, currentNode, 0, nodeSize);
		ByteBuffer wrapper = ByteBuffer.wrap(currentNode);

		nodeSearch:
		while(true) {
			int keysAmount = wrapper.getInt();

			int checkChild = keysAmount;

			goThroughChilds:
			for(int i = 0, j = IB+IB; i != keysAmount; i++, j+= keyLength+IB+IB) {
				switch(compare(key, currentNode, j)) {
					case -1:
						checkChild = i;
						break goThroughChilds;
					case 0:
						return getList(currentNode, wrapper, i);
				}
			}

			if(pickNode(wrapper, checkChild))
				return null;
		}
	}

	public final void add(final byte[] key, final int value) {
		byte[] currentNode = new byte[nodeSize+keyLength+IB+IB]; // Дополнительная длина добавленна для пронстранства при расширении
		System.arraycopy(root, 0, currentNode, 0, nodeSize);
		ByteBuffer wrapper = ByteBuffer.wrap(currentNode);

		Stack<Integer> jumps = new Stack<Integer>();
		Stack<Integer> points = new Stack<Integer>();
		jumps.push(rootPos);

		if(findInsertPosition(key, value, jumps, points, currentNode, wrapper))
			return; // Значение уже добавленно

		final int listNode = createNode(value);

		recursiveInsertion(key.clone(), listNode, jumps, points, currentNode, wrapper);
	}

	private void recursiveInsertion(byte[] key, int listValue, final Stack<Integer> jumps, final Stack<Integer> points, final byte[] currentNode, final ByteBuffer wrapper) {
		try {
			int leftBranchPointer = -1;
			int rightBranchPointer = -1;

			int keysAmount = wrapper.getInt(0);

			int insertPoint = points.pop();

			int currentNodeOffset = jumps.pop();

			while(true) {
				// Сместить
				System.arraycopy(
					currentNode, IB + insertPoint*(IB+keyLength+IB)+IB,
					currentNode, IB + (insertPoint+1)*(IB+keyLength+IB)+IB,
					(keysAmount*(IB+keyLength+IB)+IB - insertPoint*(IB+keyLength+IB)-IB));

				// Вставить ключ
				System.arraycopy(key, 0, currentNode,
					IB + insertPoint*(IB+keyLength+IB)+IB, keyLength);

				// Вставить ветви
				wrapper.putInt(IB + insertPoint*(IB+keyLength+IB), leftBranchPointer);
				wrapper.putInt(IB + (insertPoint+1)*(IB+keyLength+IB), rightBranchPointer);

				// Вставить значение
				wrapper.putInt(IB + insertPoint*(IB+keyLength+IB)+IB+keyLength, listValue);

				if (keysAmount != maxKeySize) { // Нод не заполнен
					// Увеличить число ключей
					keysAmount++;

					wrapper.putInt(0, keysAmount);

					raf.seek(currentNodeOffset);
					raf.write(currentNode, 0, IB + keysAmount*(IB+keyLength+IB)+IB);

					if(jumps.isEmpty()) {
						System.arraycopy(currentNode, 0, root, 0, IB + keysAmount*(IB+keyLength+IB)+IB);
						if(rootPos != currentNodeOffset) {
							rootPos = currentNodeOffset;
							raf.seek(0);
							raf.writeInt(rootPos);
						}
					}

					return;
				}

				// Split

				int midId = maxKeySize / 2;

				listValue = wrapper.getInt(IB + midId*(IB+keyLength+IB)+IB+keyLength);

				System.arraycopy(currentNode, IB + midId*(IB+keyLength+IB)+IB, key, 0, keyLength);

				wrapper.putInt(0, minKeySize);

				leftBranchPointer = currentNodeOffset;

				raf.seek(leftBranchPointer);
				raf.write(currentNode, 0, IB + minKeySize*(IB+keyLength+IB)+IB);

				// Сместить влево
				System.arraycopy(
					currentNode, IB + (minKeySize+1)*(IB+keyLength+IB),
					currentNode, IB,
					(minKeySize*(IB+keyLength+IB)+IB));

				rightBranchPointer = nextTreeNode();

				raf.seek(rightBranchPointer);
				raf.write(currentNode, 0, IB + minKeySize*(IB+keyLength+IB)+IB);

				if(jumps.isEmpty()) { // split root
					currentNodeOffset = nextTreeNode();

					keysAmount = 0;

					wrapper.position(0);
					wrapper.putInt(keysAmount);
					wrapper.putInt(-1);

					insertPoint = 0;
				} else {
					currentNodeOffset = jumps.pop();

					raf.seek(currentNodeOffset);
					raf.readFully(currentNode, 0, nodeSize);

					keysAmount = wrapper.getInt(0);

					insertPoint = points.pop();
				}
			}
		} catch(IOException e) {
			throw new AssertionError(e.getMessage(), e);
		}
	}

	/**
		@return получилось ли добавить значение в уже существующий список.
	*/
	private boolean findInsertPosition(final byte[] key, final int value, final Stack<Integer> jumps, final Stack<Integer> points, byte[] currentNode, ByteBuffer wrapper) {
		int keysAmount;

		int insertPoint;

		while(true) {
			wrapper.position(0);

			keysAmount = wrapper.getInt();

			insertPoint = keysAmount;

			searchForPlace:
			for(int i = 0, j = IB+IB; i != keysAmount; i++, j += keyLength+IB+IB) {
				switch(compare(key, currentNode, j)) {
					case -1:
						insertPoint = i;
						break searchForPlace;
					case 0:
						addToList(wrapper, i, value);
						return true;
				}
			}

			points.push(insertPoint);

			int pos = wrapper.getInt(IB+ insertPoint*(IB+keyLength+IB));
			if(pos == -1)
				return false;

			jumps.push(pos);

			try {
				raf.seek(pos);
				raf.readFully(currentNode, 0, nodeSize);
			} catch(IOException e) {
				throw new AssertionError(e.getMessage(), e);
			}
		}
	}

	private int createNode(final int value) {
		final ByteBuffer wrapper = ByteBuffer.allocate(2*IB);
		wrapper.putInt(value);
		wrapper.putInt(-1);

		final int listNode = nextListNode();

		try {
			raf.seek(listNode);
			raf.write(wrapper.array());
		} catch(IOException e) {
			throw new AssertionError(e.getMessage(), e);
		}

		return listNode;
	}

	private void addToList(final ByteBuffer wrapper, int child, final int value) {
		final byte[] buffer = wrapper.array();

		int npos = wrapper.getInt(IB + IB + keyLength + child*(IB+IB+keyLength));
		int cpos;

		try {
			do {
				cpos = npos;

				raf.seek(cpos);
				raf.readFully(buffer, 0, 2*IB);
				wrapper.position(0);

				if(wrapper.getInt() == value) // Если элемент уже есть в списке
					return;

				npos = wrapper.getInt(); // Следующий узел
			} while(npos != -1);

			npos = nextListNode();

			raf.seek(cpos+IB);
			raf.writeInt(npos);

			wrapper.position(0);
			wrapper.putInt(value);
			wrapper.putInt(-1);

			raf.seek(npos);
			raf.write(buffer, 0, 2*IB);
		} catch(IOException e) {
			throw new AssertionError(e.getMessage(), e);
		}
	}

	private int nextTreeNode() {
		try {
			if (emptyNodePos == -1) {
				final int ret = lastPos;

				lastPos += nodeSize;

				// Записать новое значение
				raf.seek(IB);
				raf.writeInt(lastPos);

				// Padding
				if (lastPos > raf.length())
					raf.setLength(lastPos);

				return ret;
			} else {
				final int ret = emptyNodePos;

				raf.seek(emptyNodePos);

				emptyNodePos = raf.readInt();

				raf.seek(2 * IB);
				raf.writeInt(emptyNodePos);

				return ret;
			}
		} catch(IOException e) {
			throw new AssertionError(e.getMessage(), e);
		}
	}

	private int nextListNode() {
		try {
			if(emptyListPos == -1) {
				final int ret = lastPos;
				lastPos += 2*IB;
				raf.seek(IB);
				raf.writeInt(lastPos);

				return ret;
			} else {
				final int ret = emptyListPos;

				raf.seek(emptyListPos);

				emptyListPos = raf.readInt();

				raf.seek(3*IB);
				raf.writeInt(emptyListPos);

				return ret;
			}
		} catch(IOException e) {
			throw new AssertionError(e.getMessage(), e);
		}
	}

	public int[] getOrAddIfNotExists(final byte[] key, final int value) {
		// TODO:
		return null;
	}

	public void remove(final byte[] key, final int value) {
		// TODO:
	}

	private int[] getList(final byte[] buffer, final ByteBuffer wrapper, final int child) {
		int pos = wrapper.getInt(IB+IB+ child*(keyLength+IB+IB)+keyLength);

		int preRetSize = 4;
		int[] preRet = new int[preRetSize];
		int preRetPositon = 0;

		while(pos != -1) {
			try {
				raf.seek(pos);
				raf.readFully(buffer, 0, 2*IB);
			} catch (final IOException e) {
				throw new AssertionError(e.getMessage(), e);
			}
			wrapper.position(0);

			preRet[preRetPositon++] = wrapper.getInt();
			if(preRetPositon == preRetSize) {
				preRetSize *= 2;
				preRet = Arrays.copyOf(preRet, preRetSize);
			}

			pos = wrapper.getInt();
		}

		return Arrays.copyOf(preRet, preRetPositon);
	}

	private boolean pickNode(final ByteBuffer wrapper, final int branch) {
		int pos = wrapper.getInt(IB+ branch*(IB+keyLength+IB));
		if(pos == -1)
			return true;

		try {
			raf.seek(pos);
			raf.readFully(wrapper.array());
		} catch(final IOException e) {
			throw new AssertionError(e.getMessage(), e);
		}

		wrapper.position(0);

		return false;
	}

	private byte compare(final byte[] key, final byte[] with, int offset) {
		for(int i = 0; i != keyLength; i++, offset++) {
			if(key[i] < with[offset])
				return -1; // less

			if(key[i] > with[offset])
				return 1; // bigger
		}
		return 0; // same
	}
}