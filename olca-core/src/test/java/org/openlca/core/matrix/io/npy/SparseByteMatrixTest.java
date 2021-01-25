package org.openlca.core.matrix.io.npy;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.util.Random;

import org.junit.Test;
import org.openlca.core.matrix.format.ByteMatrixBuffer;
import org.openlca.core.matrix.format.HashPointByteMatrix;

public class SparseByteMatrixTest {

	@Test
	public void testCSCStorage() throws Exception {

		int dim = 100_000;
		int n = 10_000;

		// generate the data
		int[] rows = new int[n];
		int[] cols = new int[n];
		byte[] data = new byte[n];
		var rand = new Random();
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
		var m = buffer.finish();
		assertTrue(m instanceof HashPointByteMatrix);

		// write the CSC matrix to a file
		var csc = ((HashPointByteMatrix) m).compress();
		var file = Files.createTempFile("_olca_test", ".npz").toFile();
		Npz.save(file, csc);

		// load the data from the CSC matrix and check it
		var h = new HashPointByteMatrix();
		Npz.loadByteMatrix(file).iterate(h::set);
		assertEquals(n, h.getNumberOfEntries());
		for (int i = 0; i < n; i++) {
			assertEquals(data[i], h.get(rows[i], cols[i]));
		}

		Files.delete(file.toPath());
	}

}
