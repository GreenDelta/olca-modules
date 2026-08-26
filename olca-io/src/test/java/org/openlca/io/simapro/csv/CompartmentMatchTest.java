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

		if (true)
			return;

		// emissions to soil
		check("Emission to soil",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.UNSPECIFIED);
		check("Emission to soil/agricultural",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.SOIL_AGRICULTURAL);
		check("Emission to soil/forestry",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.SOIL_FORESTRY);
		check("Emission to soil/industrial",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.SOIL_INDUSTRIAL);
		check("Emission to soil/unspecified",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.UNSPECIFIED);
		check("Emission to soil/urban, non industrial",
			ElementaryFlowType.EMISSIONS_TO_SOIL, SubCompartment.SOIL_URBAN);

		// emissions to water
		check("Emission to water",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.UNSPECIFIED);

		// "Emission to water/fossil-" is the ecoinvent name for water of fossil
		// origin; it corresponds to the SimaPro sub-compartment "fossilwater".
		check("Emission to water/fossil-",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_FOSSIL);

		// "Emission to water/fresh water" and "Emission to water/fresh water,
		// long-term" have no exact SimaPro counterpart. SimaPro does not define a
		// "fresh water" sub-compartment; it splits fresh water into the specific
		// sub-compartments river, lake, and groundwater. As the target
		// sub-compartment cannot be derived from the category, it is mapped to
		// the "unspecified" sub-compartment of the water emissions flow type.
		check("Emission to water/fresh water",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.UNSPECIFIED);
		check("Emission to water/fresh water, long-term",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.UNSPECIFIED);

		check("Emission to water/ground water",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_GROUND);
		check("Emission to water/ground water, long-term",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_GROUND_LONG_TERM);
		check("Emission to water/lake",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_LAKE);
		check("Emission to water/ocean",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_OCEAN);
		check("Emission to water/river",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_RIVER);
		check("Emission to water/river, long-term",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.WATER_RIVER_LONG_TERM);

		// "Emission to water/surface water" has no exact SimaPro counterpart.
		// SimaPro does not define a "surface water" sub-compartment; surface
		// water is covered by the sub-compartments river and lake. As the target
		// cannot be derived, it is mapped to the "unspecified" sub-compartment.
		check("Emission to water/surface water",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.UNSPECIFIED);
		check("Emission to water/unspecified",
			ElementaryFlowType.EMISSIONS_TO_WATER, SubCompartment.UNSPECIFIED);

		// "Immaterial emission" is the ecoinvent name for flows that have no
		// mass; it corresponds to the SimaPro elementary flow type
		// "Non material emissions" (e.g. noise, radiation, land transformation).
		// SimaPro defines no sub-compartment for this flow type.
		check("Immaterial emission",
			ElementaryFlowType.NON_MATERIAL_EMISSIONS, SubCompartment.UNSPECIFIED);
		check("Immaterial emission/unspecified",
			ElementaryFlowType.NON_MATERIAL_EMISSIONS, SubCompartment.UNSPECIFIED);

		// resources
		check("Resource",
			ElementaryFlowType.RESOURCES, SubCompartment.UNSPECIFIED);
		check("Resource/biotic",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_BIOTIC);

		// "Resource/fossil well" has no exact SimaPro counterpart. The SimaPro
		// resource sub-compartments are only: in air, in ground, in water, land,
		// and biotic. A fossil well is not covered by any of them, so the
		// category is mapped to the "unspecified" sub-compartment of the
		// resource flow type.
		check("Resource/fossil well",
			ElementaryFlowType.RESOURCES, SubCompartment.UNSPECIFIED);

		check("Resource/in air",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_IN_AIR);
		check("Resource/in ground",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_IN_GROUND);
		check("Resource/in water",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_IN_WATER);
		check("Resource/land",
			ElementaryFlowType.RESOURCES, SubCompartment.RESOURCES_LAND);
		check("Resource/unspecified",
			ElementaryFlowType.RESOURCES, SubCompartment.UNSPECIFIED);

		// waste flows
		check("Waste",
			ElementaryFlowType.FINAL_WASTE_FLOWS, SubCompartment.UNSPECIFIED);

		// "Waste/ecopoints 97, CH" is a Swiss ecological scarcity (ecopoints 97)
		// waste category from the ecoinvent reference data and has no SimaPro
		// counterpart. SimaPro final waste flows do not define any
		// sub-compartments, so the category is mapped to the "unspecified"
		// sub-compartment of the final waste flows type.
		check("Waste/ecopoints 97, CH",
			ElementaryFlowType.FINAL_WASTE_FLOWS, SubCompartment.UNSPECIFIED);
		check("Waste/unspecified",
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
