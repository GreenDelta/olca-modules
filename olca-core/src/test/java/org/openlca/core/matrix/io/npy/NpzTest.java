package org.openlca.core.matrix.io.npy;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.io.NpyMatrix;
import org.openlca.util.Dirs;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class NpzTest {

	@Test
	public void testCSC() throws IOException {

		// construct the CSC matrix
		var m = CSCMatrix.of(new double[][]{
			{0.0, 0.0, 0.0, 0.0},
			{0.0, 4.0, 0.0, 0.0},
			{0.0, 0.0, 0.0, 1.0},
			{0.0, 0.0, 0.0, 0.0},
			{0.0, 0.0, 0.0, 0.0},
		});

		// the check routine
		Consumer<CSCMatrix> checkCSC = (csc) -> {
			Assert.assertEquals(5, csc.rows);
			Assert.assertEquals(4, csc.columns);
			Assert.assertArrayEquals(new int[]{0, 0, 1, 1, 2}, csc.columnPointers);
			Assert.assertArrayEquals(new double[]{4.0, 1.0}, csc.values, 1e-16);
			Assert.assertArrayEquals(new int[]{1, 2}, csc.rowIndices);
		};
		checkCSC.accept(m);

		var tmpDir = Files.createTempDirectory("_npy_olca").toFile();
		var file = NpyMatrix.write(tmpDir, "M", m);
		var copy = NpyMatrix.read(file);
		Assert.assertTrue(copy instanceof CSCMatrix);
		checkCSC.accept((CSCMatrix) copy);
		Dirs.delete(tmpDir);
	}
}
