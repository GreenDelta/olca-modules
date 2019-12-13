package org.openlca.core.matrix.io.npy;

import java.io.IOException;
import java.nio.ByteBuffer;

@FunctionalInterface
interface BufferAllocator {

	/**
	 * Needs to allocate a byte buffer with at least the given size.
	 */
	ByteBuffer allocate(int size) throws IOException;

}
