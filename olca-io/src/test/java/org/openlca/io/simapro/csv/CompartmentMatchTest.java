package org.openlca.io.simapro.csv;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.SubCompartment;

public class CompartmentMatchTest {

	@Test
	public void testMatching() {

		// economic issues
		check("Elementary flows/Economic",
			ElementaryFlowType.ECONOMIC_ISSUES, SubCompartment.UNSPECIFIED);
		check("Elementary flows/Economic/unspecified",
			ElementaryFlowType.ECONOMIC_ISSUES, SubCompartment.UNSPECIFIED);

		// emissions to air
		check("Elementary flows/Emission to air",
			ElementaryFlowType.EMISSIONS_TO_AIR, SubCompartment.UNSPECIFIED);
		check("Elementary flows/Emission to air/high population density",
			ElementaryFlowType.EMISSIONS_TO_AIR, SubCompartment.AIR_HIGH_POP);
		check("Elementary flows/Emission to air/indoor",
			ElementaryFlowType.EMISSIONS_TO_AIR, SubCompartment.AIR_INDOOR);
		check("Elementary flows/Emission to air/low population density",
			ElementaryFlowType.EMISSIONS_TO_AIR, SubCompartment.AIR_LOW_POP);
		check("Elementary flows/Emission to air/low population density, long-term",
			ElementaryFlowType.EMISSIONS_TO_AIR, SubCompartment.AIR_LOW_POP_LONG_TERM);
		check("Elementary flows/Emission to air/lower stratosphere + upper troposphere",
			ElementaryFlowType.EMISSIONS_TO_AIR, SubCompartment.AIR_STRATOSPHERE_TROPOSPHERE);
		check("Elementary flows/Emission to air/stratosphere",
			ElementaryFlowType.EMISSIONS_TO_AIR, SubCompartment.AIR_STRATOSPHERE);
		check("Elementary flows/Emission to air/unspecified",
			ElementaryFlowType.EMISSIONS_TO_AIR, SubCompartment.UNSPECIFIED);

		// emissions to soil
		check("Elementary flows/Emission to soil",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.UNSPECIFIED);
		check("Elementary flows/Emission to soil/agricultural",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.SOIL_AGRICULTURAL);
		check("Elementary flows/Emission to soil/forestry",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.SOIL_FORESTRY);
		check("Elementary flows/Emission to soil/industrial",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.SOIL_INDUSTRIAL);
		check("Elementary flows/Emission to soil/unspecified",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.UNSPECIFIED);
		check("Elementary flows/Emission to soil/urban, non industrial",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.SOIL_URBAN);

		// emissions to water
		check("Elementary flows/Emission to water",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.UNSPECIFIED);
		check("Elementary flows/Emission to water/fossil-",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_FOSSIL);

		// "Emission to water/fresh water" -> "...river..."!
		check("Elementary flows/Emission to water/fresh water",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_RIVER);
		check("Elementary flows/Emission to water/fresh water, long-term",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_RIVER_LONG_TERM);

		check("Elementary flows/Emission to water/ground water",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_GROUND);
		check("Elementary flows/Emission to water/ground water, long-term",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_GROUND_LONG_TERM);
		check("Elementary flows/Emission to water/lake",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_LAKE);
		check("Elementary flows/Emission to water/ocean",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_OCEAN);
		check("Elementary flows/Emission to water/river",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_RIVER);
		check("Elementary flows/Emission to water/river, long-term",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_RIVER_LONG_TERM);

		// "Emission to water/surface water"  -> "...river..."!
		check("Elementary flows/Emission to water/surface water",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_RIVER);
		check("Elementary flows/Emission to water/unspecified",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.UNSPECIFIED);

		// immaterial emissions
		check("Elementary flows/Immaterial emission",
			ElementaryFlowType.NON_MATERIAL_EMISSIONS, SubCompartment.UNSPECIFIED);
		check("Elementary flows/Immaterial emission/unspecified",
			ElementaryFlowType.NON_MATERIAL_EMISSIONS, SubCompartment.UNSPECIFIED);

		// resources
		check("Elementary flows/Resource",
			ElementaryFlowType.RESOURCES, SubCompartment.UNSPECIFIED);
		check("Elementary flows/Resource/biotic",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_BIOTIC);

		// "Resource/fossil well" -> "...in ground..."
		check("Elementary flows/Resource/fossil well",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_IN_GROUND);

		check("Elementary flows/Resource/in air",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_IN_AIR);
		check("Elementary flows/Resource/in ground",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_IN_GROUND);
		check("Elementary flows/Resource/in water",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_IN_WATER);
		check("Elementary flows/Resource/land",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_LAND);
		check("Elementary flows/Resource/unspecified",
			ElementaryFlowType.RESOURCES, SubCompartment.UNSPECIFIED);

		// waste flows
		check("Elementary flows/Waste",
			ElementaryFlowType.FINAL_WASTE_FLOWS, SubCompartment.UNSPECIFIED);
		check("Elementary flows/Waste/ecopoints 97, CH",
			ElementaryFlowType.FINAL_WASTE_FLOWS, SubCompartment.UNSPECIFIED);
		check("Elementary flows/Waste/unspecified",
			ElementaryFlowType.FINAL_WASTE_FLOWS, SubCompartment.UNSPECIFIED);
	}

	private void check(String path, ElementaryFlowType type, SubCompartment sub) {

		var ps = List.of(
			path,
			path.toUpperCase(Locale.ROOT),
			path.toLowerCase(Locale.ROOT));
		for (var p : ps) {
			var comp = Compartment.fromPath(p);
			assertNotNull("No compartment found for: " + path, comp);
			assertEquals("Unexpected type for: " + path, type, comp.type());
			assertEquals("Unexpected sub-compartment for: " + path, sub, comp.sub());
		}

		Category category = null;
		for (var seg : path.split("/")) {
			var c = Category.of(seg.strip(), ModelType.FLOW);
			if (category != null) {
				category.childCategories.add(c);
				c.category = category;
			}
			category = c;
		}

		var comp = Compartment.of(category);
		assertNotNull("No compartment found for: " + path, comp);
		assertEquals("Unexpected type for: " + path, type, comp.type());
		assertEquals("Unexpected sub-compartment for: " + path, sub, comp.sub());
	}

}
