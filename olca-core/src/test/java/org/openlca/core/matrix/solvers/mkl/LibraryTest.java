package org.openlca.core.matrix.solvers.mkl;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LibraryTest {

	@Test
	public void testIsLibFolder() {
		assertTrue(MKL.isDefaultLibraryDir());
		assertTrue(MKL.loadFromDefault());
	}

}
