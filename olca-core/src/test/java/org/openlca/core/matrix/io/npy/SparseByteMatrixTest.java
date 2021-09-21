package org.openlca.core.matrix.io.npy;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;
import org.openlca.core.matrix.format.ByteMatrixBuffer;
import org.openlca.core.matrix.format.CSCByteMatrix;
import org.openlca.core.matrix.format.HashPointByteMatrix;
import org.openlca.core.matrix.io.NpyMatrix;
import org.openlca.util.Dirs;

public class SparseByteMatrixTest {

	@Test
	public void testCSCStorage() throws Exception {

		int dim = 100_000;
		int n = 10_000;

		// generate the data
		int[] rows = new int[n];
		int[] cols = new int[n];
		byte[] data = new byte[n];
		var rand = ThreadLocalRandom.current();
		for (int i = 0; i < n; i++) {
			rows[i] = rand.nextInt(dim);
			cols[i] = rand.nextInt(dim);
			data[i] = (byte) (rand.nextInt(5) + 1);
		}

		// use the same builders that we would use for
		// building the DQI matrices of a database
		var buffer = new ByteMatrixBuffer();
		buffer.minSize(dim, dim);
		for (int i = 0; i < n; i++) {
			buffer.set(rows[i], cols[i], data[i]);
		}
		var m = (HashPointByteMatrix) buffer.finish();
		var numberOfEntries = m.getNumberOfEntries();

		// write the matrix to a file; this should write it
		// in the CSC format
		var dir = Files.createTempDirectory("_olca_test").toFile();
		NpyMatrix.write(dir, "M", m);

		// load the data from the CSC matrix and check it
		var h = new HashPointByteMatrix();
		NpyMatrix.readBytes(dir, "M")
			.map(CSCByteMatrix.class::cast)
			.orElseThrow()
			.iterate(h::set);
		assertEquals(numberOfEntries, h.getNumberOfEntries());
		for (int i = 0; i < n; i++) {
			assertEquals(data[i], h.get(rows[i], cols[i]));
		}

		Dirs.delete(dir);
	}
}
