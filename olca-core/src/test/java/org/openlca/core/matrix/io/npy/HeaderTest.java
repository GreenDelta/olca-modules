package org.openlca.core.matrix.io.npy;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.format.DenseMatrix;
import sun.misc.Cleaner;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

public class HeaderTest {

	@Test
	public void testParseString() {
		String s = "{'descr': '<i4', 'fortran_order': False, 'shape': (2,), }";
		Header h = Header.read(s);
		Assert.assertEquals("<i4", h.dtype);
		Assert.assertArrayEquals(new int[]{2}, h.shape);
		Assert.assertFalse(h.fortranOrder);
		Assert.assertEquals(s, h.toString());
	}

	@Test
	public void testFromReadFile() throws Exception {
		DenseMatrix m = new DenseMatrix(2, 3);
		File file = Files.createTempFile("_olca_npy_tests", ".npy").toFile();
		System.out.println(file.getAbsolutePath());
		Npy.save(file, m);
		Header h = Header.read(file);
		Header h2 = Header.read(file);
		Assert.assertEquals(h.toString(), h2.toString());
		Assert.assertEquals("<f8", h.dtype);
		Assert.assertTrue(h.fortranOrder);
		Assert.assertArrayEquals(new int[]{2, 3}, h.shape);
		Assert.assertTrue(file.delete());
	}

	@Test
	public void testDeleteTempFile() throws Exception {
		File file = Files.createTempFile("_file_del_test", ".txt").toFile();
		try (RandomAccessFile f = new RandomAccessFile(file, "rw");
		     FileChannel chan = f.getChannel()) {
			MappedByteBuffer buf = chan.map(
					FileChannel.MapMode.READ_WRITE, 0, 42);
			for (int i = 0; i < 42; i++) {
				buf.put((byte) 42);
			}
			buf.force();
			Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buf).cleaner();
			if (cleaner != null) {
				cleaner.clean();
			}
		}
		Assert.assertTrue(file.delete());
	}

}
