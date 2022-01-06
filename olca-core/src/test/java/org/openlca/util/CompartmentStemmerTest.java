package org.openlca.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CompartmentStemmerTest {

	private final CompartmentStemmer stemmer = new CompartmentStemmer();

	@Test
	public void testOpenLCA() {
		test("Elementary flows", "");
		test("Elementary flows/Emission to air", "air");
		test("Elementary flows/Emission to air/high population density",
			"air/high,pop");
		test("Elementary flows/Emission to air/low population density",
			"air/low,pop");
		test("Elementary flows/Emission to air/low population density, long-term",
			"air/long,low,pop,term");
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

	@Test
	public void testSimaPro() {

		var air = new String[] {
			"Air", "Airborne emissions", "Emissions to air"};
		for (var top : air) {
			test(top + "/(unspecified)", "air");
			test(top + "/high. pop.", "air/high,pop");
			test(top + "/indoor", "air/indoor");
			test(top + "/low. pop.", "air/low,pop");
			test(top + "/low. pop., long-term", "air/long,low,pop,term");
			test(top + "/stratosphere", "air/stratosphere");
			test(top + "/stratosphere + troposhere", "air/stratosphere,troposhere");
		}

		test("Economic issues/(unspecified)", "economic");
		test("Economic/(unspecified)", "economic");

		var soil = new String[] {
			"Soil", "Emissions to soil"};
		for (var top : soil) {
			test(top + "/(unspecified)", "soil");
			test(top + "/agricultural", "soil/agricultural");
			test(top + "/forestry", "soil/forestry");
			test(top + "/industrial", "soil/industrial");
			test(top + "/urban, non industrial", "soil/industrial,non,urban");
		}

		var water = new String[] {
			"Emissions to water", "Water", "Waterborne emissions"};
		for (var top : water) {
			test(top + "/(unspecified)", "water");
			test(top + "/fossilwater", "water/fossil");
			test(top + "/groundwater", "water/ground");
			test(top + "/groundwater, long-term", "water/ground,long,term");
			test(top + "/lake", "water/lake");
			test(top + "/ocean", "water/ocean");
			test(top + "/river", "water/river");
			test(top + "/river, long-term", "water/long,river,term");
		}

		test("Final waste flows/(unspecified)", "waste");
		test("Waste/(unspecified)", "waste");

		test("Non mat./(unspecified)", "material,non");
		test("Non material emissions/(unspecified)", "material,non");

		var resource = new String[] {
			"Raw materials", "Raw", "Resources"};
		for (var top : resource) {
			test(top + "/(unspecified)", "resource");
			test(top + "/biotic", "resource/biotic");
			test(top + "/in air", "resource/air");
			test(top + "/in ground", "resource/ground");
			test(top + "/in water", "resource/water");
			test(top + "/land", "resource/land");
		}

		test("Social issues/(unspecified)", "social");
		test("Social/(unspecified)", "social");

	}

	private void test(String path, String expected) {
		var stemmed = stemmer.stem(path);
		var joined = String.join("/", stemmed);
		assertEquals(expected, joined);
	}

}
