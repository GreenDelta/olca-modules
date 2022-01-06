package org.openlca.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CompartmentStemmerTest {

	private final CompartmentStemmer stemmer = new CompartmentStemmer();

	@Test
	public void testOpenLCA() {
		test("Elementary flows", "");
		test("Elementary flows/Emission to air", "air");
		test("Elementary flows/Emission to air/high population density", "");
		test("Elementary flows/Emission to air/low population density", "");
		test("Elementary flows/Emission to air/low population density, long-term", "");
		test("Elementary flows/Emission to air/lower stratosphere + upper troposphere", "");
		test("Elementary flows/Emission to air/unspecified", "");
		test("Elementary flows/Emission to soil", "");
		test("Elementary flows/Emission to soil/agricultural", "");
		test("Elementary flows/Emission to soil/forestry", "");
		test("Elementary flows/Emission to soil/industrial", "");
		test("Elementary flows/Emission to soil/unspecified", "");
		test("Elementary flows/Emission to water", "");
		test("Elementary flows/Emission to water/ground water", "");
		test("Elementary flows/Emission to water/ground water, long-term", "");
		test("Elementary flows/Emission to water/ocean", "");
		test("Elementary flows/Emission to water/surface water", "");
		test("Elementary flows/Emission to water/unspecified", "");
		test("Elementary flows/Resource", "");
		test("Elementary flows/Resource/biotic", "");
		test("Elementary flows/Resource/in air", "");
		test("Elementary flows/Resource/in ground", "");
		test("Elementary flows/Resource/in water", "");
		test("Elementary flows/Resource/land", "");
	}

	private void test(String path, String expected) {
		var stemmed = stemmer.stem(path);
		var joined = String.join("/", stemmed);
		assertEquals(expected, joined);
	}

}
