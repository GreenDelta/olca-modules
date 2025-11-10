package org.openlca.core.matrix.solvers.mkl;

import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Test;

public class LibraryTest {

	@Test
	public void testIsLibFolder() {
		Assume.assumeTrue(MKL.loadFromDefault());
		assertTrue(MKL.isDefaultLibraryDir());
		assertTrue(MKL.loadFromDefault());
	}

}
