package org.openlca.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringsTest {

	@Test
	public void testCutEnd() {
		assertEquals("", Strings.cutEnd(null, 42));
		assertEquals("", Strings.cutEnd("", 42));
		assertEquals("", Strings.cutEnd(" ", 42));
		assertEquals("", Strings.cutEnd("test", 0));
		assertEquals("", Strings.cutEnd("test", -5));

		assertEquals(".", Strings.cutEnd("abc", 1));
		assertEquals("..", Strings.cutEnd("abc", 2));
		assertEquals("abc", Strings.cutEnd("abc", 3));
		assertEquals("abc", Strings.cutEnd(" abc ", 3));
		assertEquals("abcd", Strings.cutEnd("abcd", 4));
		assertEquals("ab...", Strings.cutEnd("abcdef", 5));
		assertEquals("This i...", Strings.cutEnd("This is a long text", 9));
		assertEquals("Hello World", Strings.cutEnd("  Hello World  ", 20));
	}

	@Test
	public void testCutStart() {
		assertEquals("", Strings.cutStart(null, 42));
		assertEquals("", Strings.cutStart("", 42));
		assertEquals("", Strings.cutStart(" ", 42));
		assertEquals("", Strings.cutStart("test", 0));
		assertEquals("", Strings.cutStart("test", -5));

		assertEquals(".", Strings.cutStart("abc", 1));
		assertEquals("..", Strings.cutStart("abc", 2));
		assertEquals("abc", Strings.cutStart("abc", 3));
		assertEquals("abc", Strings.cutStart(" abc ", 3));
		assertEquals("abcd", Strings.cutStart("abcd", 4));
		assertEquals("...efg", Strings.cutStart("abcdefg", 6));
		assertEquals("...g text", Strings.cutStart("This is a long text", 9));
		assertEquals("Hello World", Strings.cutStart("  Hello World  ", 20));
	}

	@Test
	public void testCutMid() {
		assertEquals("", Strings.cutMid(null, 42));
		assertEquals("", Strings.cutMid("", 42));
		assertEquals("", Strings.cutMid(" ", 42));
		assertEquals("", Strings.cutMid("test", 0));
		assertEquals("", Strings.cutMid("test", -5));

		assertEquals(".", Strings.cutMid("abc", 1));
		assertEquals("..", Strings.cutMid("abc", 2));

		assertEquals("a", Strings.cutMid(" a ", 3));
		assertEquals("ab", Strings.cutMid(" ab ", 3));
		assertEquals("abc", Strings.cutMid(" abc ", 3));
		assertEquals("abcd", Strings.cutMid("abcd", 4));
		assertEquals("abcdef", Strings.cutMid("abcdef", 9));

		assertEquals("abcdef", Strings.cutMid("abcdef", 6));
		assertEquals("a...f", Strings.cutMid("abcdef", 5));
		assertEquals("a...", Strings.cutMid("abcdef", 4));
		assertEquals("...", Strings.cutMid("abcdef", 3));

		assertEquals("This...ext", Strings.cutMid("This is a long text", 10));
		assertEquals("This...text", Strings.cutMid("This is a long text", 11));
		assertEquals("Hello World", Strings.cutMid("  Hello World  ", 20));
	}

}
