package org.openlca.julia;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class LibraryDownloadTest {

	/**
	 * This test is ignored as we only run it from time to time in order
	 * to check if our library downloads work. Before running the test,
	 * delete the `~./openLCA` folder. Otherwise you may do not really
	 * test something here.
	 */
	@Test
	@Ignore
	public void testFetchSparseLibs() {

		// first load the libraries from the jar
		Assert.assertTrue(Julia.load());
		Assert.assertTrue(Julia.isLoaded());
		Assert.assertFalse(Julia.hasSparseLibraries());

		// now fetch the sparse libraries from the web
		Assert.assertTrue(Julia.fetchSparseLibraries());
		Assert.assertTrue(Julia.isLoaded());
		Assert.assertTrue(Julia.hasSparseLibraries());
	}
}
