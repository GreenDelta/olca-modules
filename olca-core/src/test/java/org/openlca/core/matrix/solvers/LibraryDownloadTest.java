package org.openlca.core.matrix.solvers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.DataDir;
import org.openlca.nativelib.Module;
import org.openlca.nativelib.NativeLib;

public class LibraryDownloadTest {

	/**
	 * This test is ignored as we only run it from time to time in order to check
	 * if our library downloads work. Before running the test, delete the
	 * `~./<workspace>/olca-native` folder. Otherwise, you may do not really test
	 * something here.
	 */
	@Test
	@Ignore
	public void testFetchSparseLibs() throws Exception {
		assertFalse(NativeLib.isLoaded());
		NativeLib.download(DataDir.get().root(), Module.UMFPACK);
		NativeLib.loadFrom(DataDir.get().root());
		assertTrue(NativeLib.isLoaded());
		assertTrue(NativeLib.isLoaded(Module.BLAS));
		assertTrue(NativeLib.isLoaded(Module.UMFPACK));
	}
}
