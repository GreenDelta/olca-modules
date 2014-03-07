package org.openlca.simapro.csv;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;
import org.openlca.simapro.csv.model.enums.BoundaryWithNature;
import org.openlca.simapro.csv.model.enums.CutOffRule;
import org.openlca.simapro.csv.model.enums.Geography;
import org.openlca.simapro.csv.model.enums.ProcessAllocation;
import org.openlca.simapro.csv.model.enums.ProcessCategory;
import org.openlca.simapro.csv.model.enums.ProcessType;
import org.openlca.simapro.csv.model.enums.Representativeness;
import org.openlca.simapro.csv.model.enums.Status;
import org.openlca.simapro.csv.model.enums.Substitution;
import org.openlca.simapro.csv.model.enums.Technology;
import org.openlca.simapro.csv.model.enums.TimePeriod;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.reader.BlockReader;
import org.openlca.simapro.csv.reader.ModelReader;

public class ProcessBlockTest {

	private ProcessBlock block;

	@Before
	public void setUp() throws Exception {
		try (InputStream is = this.getClass().getResourceAsStream(
				"simple_process.csv");
				InputStreamReader reader = new InputStreamReader(is);
				BlockReader blockReader = new BlockReader(reader);
				ModelReader modelReader = new ModelReader(blockReader,
						CsvConfig.getDefault(), ProcessBlock.class)) {
			this.block = (ProcessBlock) modelReader.read();
		}
	}

	@Test
	public void testTextEntries() {
		assertEquals("DefaultX25250700002", block.getIdentifier());
		assertEquals("Test process", block.getName());
		assertEquals("First order (only primary flows)",
				block.getCapitalGoods());
	}

	@Test
	public void testEnumEntries() {
		assertEquals(ProcessCategory.MATERIAL, block.getCategory());
		assertEquals(ProcessType.UNIT_PROCESS, block.getProcessType());
		assertEquals(Status.DRAFT, block.getStatus());
		assertEquals(TimePeriod.P_2005_2009, block.getTime());
		assertEquals(Geography.MIXED_DATA, block.getGeography());
		assertEquals(Technology.WORST_CASE, block.getTechnology());
		assertEquals(Representativeness.THEORETICAL_CALCULATION,
				block.getRepresentativeness());
		assertEquals(ProcessAllocation.PHYSICAL_CAUSALITY,
				block.getAllocation());
		assertEquals(Substitution.ACTUAL_SUBSTITUTION, block.getSubstitution());
		assertEquals(CutOffRule.PHYSICAL_LESS_THAN_1, block.getCutoff());
		assertEquals(BoundaryWithNature.AGRICULTURAL_PRODUCTION_SYSTEM,
				block.getBoundaryWithNature());
	}

	@Test
	public void testElementaryExchanges() {
		assertEquals("Acids", block.getResources().get(0).getName());
		assertEquals("(+-)-Citronellol", block.getEmissionsToAir().get(0)
				.getName());
		assertEquals("(1r,4r)-(+)-Camphor", block.getEmissionsToWater().get(0)
				.getName());
		assertEquals("1'-Acetoxysafrole", block.getEmissionsToSoil().get(0)
				.getName());
		assertEquals("Asbestos", block.getFinalWasteFlows().get(0).getName());
		assertEquals("Noise from bus km", block.getNonMaterialEmissions()
				.get(0).getName());
		assertEquals("venting of argon, crude, liquid", block.getSocialIssues()
				.get(0).getName());
		assertEquals("Sample economic issue", block.getEconomicIssues().get(0)
				.getName());
	}

	@Test
	public void testParameters() {
		assertEquals(1, block.getCalculatedParameters().size());
		assertEquals(1, block.getInputParameters().size());
		assertEquals("calc_param", block.getCalculatedParameters().get(0)
				.getName());
		assertEquals("input_param", block.getInputParameters().get(0).getName());
	}

}
