package org.openlca.io;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;

/** We can use the Guava joiner to join multiple texts. */
public class TextJoinerTest {

	@Test
	public void testAllNull() {
		String s1 = null, s2 = null, s3 = null, s4 = null;
		String string = Joiner.on(" ").skipNulls().join(s1, s2, s3, s4);
		Assert.assertEquals("", string);
	}

	@Test
	public void testFirstNull() {
		String s1 = null, s2 = "1", s3 = "2", s4 = "3";
		String string = Joiner.on(", ").skipNulls().join(s1, s2, s3, s4);
		Assert.assertEquals("1, 2, 3", string);
	}

	@Test
	public void testFirstLastNull() {
		String s1 = null, s2 = "1", s3 = "2", s4 = "3", s5 = null;
		String string = Joiner.on(", ").skipNulls().join(s1, s2, s3, s4, s5);
		Assert.assertEquals("1, 2, 3", string);
	}

	@Test
	public void testAllNotNull() {
		String s1 = "0", s2 = "1", s3 = "2", s4 = "3", s5 = "4";
		String string = Joiner.on(", ").skipNulls().join(s1, s2, s3, s4, s5);
		Assert.assertEquals("0, 1, 2, 3, 4", string);
	}

}
