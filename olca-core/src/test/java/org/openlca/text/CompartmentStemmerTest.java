package org.openlca.text;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompartmentStemmerTest {

	private final CompartmentStemmer stemmer = new CompartmentStemmer();

	@Test
	public void testOpenLCA() {
		var prefix = "Elementary flows";

		test(prefix, "");

		var air = "Elementary flows/Emission to air";
		test(air, "air");
		test(air + "/unspecified", "air");
		test(air + "/high population density", "air/high population");
		test(air + "/low population density", "air/low population");
		test(air + "/low population density, long-term", "air/long-term;low population");
		test(air + "/lower stratosphere + upper troposphere",
			"air/lower stratosphere;upper troposphere");

		var soil = "Elementary flows/Emission to soil";
		test(soil, "soil");
		test(soil + "/unspecified", "soil");
		test(soil + "/agricultural", "soil/agricultural");
		test(soil + "/forestry", "soil/forestry");
		test(soil + "/industrial", "soil/industrial");

		var water = "Elementary flows/Emission to water";
		test(water, "water");
		test(water + "/unspecified", "water");
		test(water + "/ground water", "water/ground");
		test(water + "/ground water, long-term", "water/ground;long-term");
		test(water + "/ocean", "water/ocean");
		test(water + "/surface water", "water/surface");

		var resource = "Elementary flows/Resource";
		test(resource, "resource");
		test(resource + "/biotic", "resource/biotic");
		test(resource + "/in air", "resource/air");
		test(resource + "/in ground", "resource/ground");
		test(resource + "/in water", "resource/water");
		test(resource + "/land", "resource/land");
	}

	@Test
	public void testSimaPro() {

		var air = new String[] {
			"Air", "Airborne emissions", "Emissions to air"};
		for (var top : air) {
			test(top + "/(unspecified)", "air");
			test(top + "/high. pop.", "air/high population");
			test(top + "/indoor", "air/indoor");
			test(top + "/low. pop.", "air/low population");
			test(top + "/low. pop., long-term", "air/long-term;low population");
			test(top + "/stratosphere", "air/stratosphere");
			test(top + "/stratosphere + troposhere", "air/stratosphere;troposhere");
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
			test(top + "/urban, non industrial", "soil/non industrial;urban");
		}

		var water = new String[] {
			"Emissions to water", "Water", "Waterborne emissions"};
		for (var top : water) {
			test(top + "/(unspecified)", "water");
			test(top + "/fossilwater", "water/fossil");
			test(top + "/groundwater", "water/ground");
			test(top + "/groundwater, long-term", "water/ground;long-term");
			test(top + "/lake", "water/lake");
			test(top + "/ocean", "water/ocean");
			test(top + "/river", "water/river");
			test(top + "/river, long-term", "water/long-term;river");
		}

		test("Final waste flows/(unspecified)", "waste");
		test("Waste/(unspecified)", "waste");

		test("Non mat./(unspecified)", "non material");
		test("Non material emissions/(unspecified)", "non material");

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
