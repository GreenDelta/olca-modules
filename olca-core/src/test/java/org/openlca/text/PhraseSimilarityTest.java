package org.openlca.text;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PhraseSimilarityTest {

	@Test
	public void test() {

		var similarity = new PhraseSimilarity();
		var parser = new PhraseParser();

		var s = similarity.get(
			parser.parse("this is the same"),
			parser.parse("this is the same"));
		assertEquals(1.0, s, 1e-10);

		var t = similarity.get(
			parser.parse("no match"),
			parser.parse("for this, right?"));
		assertEquals(0.0, t, 1e-10);

		var r = similarity.get(
			parser.parse("1200 kPa"),
			parser.parse("600 kPa"));
		assertEquals(0.5 * (3.0/7.0 + 3.0/6.0), r, 1e-10);
	}

	@Test
	public void testNoMatch() {
		var similarity = new PhraseSimilarity();
		var parser = new PhraseParser();
		var s = similarity.get(
			parser.parse("Carbon footprint"),
			parser.parse("Climate change"));
		assertEquals(0.0, s, 1e-10);
	}
}
