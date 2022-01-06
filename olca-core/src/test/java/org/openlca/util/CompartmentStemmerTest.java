package org.openlca.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompartmentStemmerTest {

	private final CompartmentStemmer stemmer = new CompartmentStemmer();

	@Test
	public void testOpenLCA() {
		test("Elementary flows",
			"");
		test("Elementary flows/Emission to air",
			"air");
		test("Elementary flows/Emission to air/high population density",
			"air/density,high,population");
		test("Elementary flows/Emission to air/low population density",
			"air/density,low,population");
		test("Elementary flows/Emission to air/low population density, long-term",
			"air/density,long,low,population,term");
		test("Elementary flows/Emission to air/lower stratosphere + upper troposphere",
			"air/lower,stratosphere,troposphere,upper");
		test("Elementary flows/Emission to air/unspecified",
			"air");
		test("Elementary flows/Emission to soil",
			"soil");
		test("Elementary flows/Emission to soil/agricultural",
			"soil/agricultural");
		test("Elementary flows/Emission to soil/forestry",
			"soil/forestry");
		test("Elementary flows/Emission to soil/industrial",
			"soil/industrial");
		test("Elementary flows/Emission to soil/unspecified",
			"soil");
		test("Elementary flows/Emission to water",
			"water");
		test("Elementary flows/Emission to water/ground water",
			"water/ground");
		test("Elementary flows/Emission to water/ground water, long-term",
			"water/ground,long,term");
		test("Elementary flows/Emission to water/ocean",
			"water/ocean");
		test("Elementary flows/Emission to water/surface water",
			"water/surface");
		test("Elementary flows/Emission to water/unspecified",
			"water");
		test("Elementary flows/Resource",
			"resource");
		test("Elementary flows/Resource/biotic",
			"resource/biotic");
		test("Elementary flows/Resource/in air",
			"resource/air");
		test("Elementary flows/Resource/in ground",
			"resource/ground");
		test("Elementary flows/Resource/in water",
			"resource/water");
		test("Elementary flows/Resource/land",
			"resource/land");
	}

	private void test(String path, String expected) {
		var stemmed = stemmer.stem(path);
		var joined = String.join("/", stemmed);
		assertEquals(expected, joined);
	}

}
