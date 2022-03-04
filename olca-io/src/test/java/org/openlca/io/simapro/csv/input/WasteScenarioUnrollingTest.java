package org.openlca.io.simapro.csv.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ProductSystem;
import org.openlca.io.Tests;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.Numeric;
import org.openlca.simapro.csv.enums.ProcessCategory;
import org.openlca.simapro.csv.enums.ProductStageCategory;
import org.openlca.simapro.csv.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.process.ProcessBlock;
import org.openlca.simapro.csv.process.ProductOutputRow;
import org.openlca.simapro.csv.process.ProductStageBlock;
import org.openlca.simapro.csv.process.ProductStageOutputRow;
import org.openlca.simapro.csv.process.TechExchangeRow;
import org.openlca.simapro.csv.process.WasteFractionRow;
import org.openlca.simapro.csv.process.WasteTreatmentRow;
import org.openlca.simapro.csv.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.refdata.QuantityRow;
import org.openlca.simapro.csv.refdata.UnitRow;

public class WasteScenarioUnrollingTest {

	private final IDatabase db = Tests.getDb();

	@Before
	public void before() {
		db.clear();
	}

	@After
	public void after() {
		db.clear();
	}

	@Test
	public void testUnrolling() throws Exception {

		var dataSet = new CsvDataSet();

		// unit: kg
		dataSet.units().add(
			new UnitRow().name("kg")
				.conversionFactor(1.0)
				.referenceUnit("kg")
				.quantity("Mass"));

		// quantity: mass
		dataSet.quantities().add(
			new QuantityRow().name("Mass")
				.hasDimension(true));

		// CO2
		dataSet.airborneEmissions().add(
			new ElementaryFlowRow()
				.name("Carbon dioxide")
				.unit("kg")
				.cas("000124-38-9")
				.comment("Formula: CO2")
				.platformId("DBE27C73-9DF4-4F21-B2EB-A0CE1FE87A4B"));

		// material
		var material = new ProcessBlock()
			.category(ProcessCategory.MATERIAL)
			.name("material 1")
			.identifier("Dev99229000009369600012");
		material.products().add(
			new ProductOutputRow()
				.name("material 1")
				.amount(Numeric.of(1.0))
				.unit("kg")
				.wasteType("Some waste"));
		dataSet.processes().add(material);

		// waste treatment
		var wasteTreatment = new ProcessBlock()
			.category(ProcessCategory.WASTE_TREATMENT)
			.name("waste treatment 1")
			.identifier("Dev99229000009369600013");
		wasteTreatment.wasteTreatment(
			new WasteTreatmentRow()
				.name("waste treatment 1")
				.amount(Numeric.of(1))
				.unit("kg"));
		wasteTreatment.emissionsToAir().add(
			new ElementaryExchangeRow()
				.name("Carbon dioxide")
				.amount(Numeric.of(1.0))
				.unit("kg"));
		dataSet.processes().add(wasteTreatment);

		// waste scenario
		var wasteScenario = new ProcessBlock()
			.category(ProcessCategory.WASTE_SCENARIO)
			.name("waste scenario 1")
			.identifier("Dev99229000009369600014");
		wasteScenario.wasteScenario(
			new WasteTreatmentRow()
				.name("waste scenario 1")
				.wasteType("All waste types")
				.amount(Numeric.of(1))
				.unit("kg"));
		wasteScenario.separatedWaste().add(
			new WasteFractionRow()
				.wasteType("All waste types")
				.fraction(100)
				.wasteTreatment("waste treatment 1"));
		wasteScenario.remainingWaste().add(
			new WasteFractionRow()
				.wasteType("All waste types")
				.fraction(100)
				.wasteTreatment("waste treatment 1"));
		dataSet.processes().add(wasteScenario);

		// assembly
		var assembly = new ProductStageBlock()
			.category(ProductStageCategory.ASSEMBLY);
		assembly.products().add(
			new ProductStageOutputRow()
				.name("assembly 1")
				.amount(Numeric.of(1))
				.unit("kg"));
		assembly.materialsAndAssemblies().add(
			new TechExchangeRow()
				.name("material 1")
				.amount(Numeric.of(2))
				.unit("kg"));
		dataSet.productStages().add(assembly);

		// life cycle
		var lifeCycle = new ProductStageBlock()
			.category(ProductStageCategory.LIFE_CYCLE);
		lifeCycle.products().add(
			new ProductStageOutputRow()
				.name("life cycle 1")
				.amount(Numeric.of(1))
				.unit("kg"));
		lifeCycle.assembly(
			new TechExchangeRow()
				.name("assembly 1")
				.amount(Numeric.of(2))
				.unit("kg"));
		lifeCycle.wasteOrDisposalScenario(
			new TechExchangeRow()
				.name("waste scenario 1"));
		dataSet.productStages().add(lifeCycle);

		// write the file
		var file = Files.createTempFile("_olca_", ".csv").toFile();
		dataSet.write(file);

		new SimaProCsvImport(db, file)
			.generateLifeCycleSystems(true)
			.unrollWasteScenarios(true)
			.run();

		var systems = db.getAll(ProductSystem.class);
		assertEquals(1, systems.size());
		var system = systems.get(0);
		assertEquals(1, system.parameterSets.size());

		var setup = CalculationSetup.simple(systems.get(0))
			.withParameters(system.parameterSets.get(0).parameters);
		var result = new SystemCalculator(db).calculateSimple(setup);
		var inventory = result.getTotalFlowResults();
		assertEquals(1, inventory.size());
		var co2Result = inventory.get(0);
		assertEquals("Carbon dioxide", co2Result.flow().name);
		assertEquals(4.0, co2Result.value(), 1e-10);

		assertTrue(file.delete());
	}
}
