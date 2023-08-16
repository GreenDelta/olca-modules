package org.openlca.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.function.Function;

import static org.junit.Assert.*;

public class StringsTest {

	@Test
	public void testUniqueNameOf() {

		var list = new ArrayList<String>();
		Function<String, String> next = s -> {
			var n = Strings.uniqueNameOf(s, list, Function.identity());
			list.add(n);
			return n;
		};

		assertEquals("Bus", next.apply("Bus"));
		assertEquals("Bus (2)", next.apply("Bus"));
		assertEquals("Auto", next.apply(" Auto  "));
		assertEquals("bus (3)", next.apply("bus"));
		assertEquals("auto (2)", next.apply("auto"));
		assertEquals("Bus (4)", next.apply("Bus"));
		assertEquals("Auto (2) (2)", next.apply("Auto (2)"));
	}
}
