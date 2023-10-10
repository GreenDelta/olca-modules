package org.openlca.io.oneclick;

import org.junit.Assert;
import org.junit.Test;

public class PackagingMatcherTest {

	@Test
	public void testMatching() {
		var matcher = PackagingMatcher.createDefault();
		var label = "market for packaging glass, green | " +
			"packaging glass, green | Cutoff, U";
		Assert.assertTrue(matcher.matches(label));
	}

	@Test
	public void testPattern() {
		var matcher = PackagingMatcher.createDefault();
		Assert.assertNotNull(matcher.pattern());
	}
}
