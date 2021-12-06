package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.Numeric;
import org.openlca.simapro.csv.enums.ProcessCategory;
import org.openlca.simapro.csv.process.ProcessBlock;
import org.openlca.simapro.csv.process.ProductOutputRow;
import org.openlca.simapro.csv.process.TechExchangeRow;
import org.openlca.simapro.csv.process.WasteFractionRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;
import org.openlca.util.Pair;
import org.openlca.util.Strings;

class WasteScenarios {

	static final String PARAMETER_PREFIX = "ws_";
	private final CsvDataSet dataSet;

	private WasteScenarios(CsvDataSet dataSet) {
		this.dataSet = dataSet;
	}

	static void unroll(CsvDataSet dataSet) {
		if (dataSet == null || dataSet.processes().isEmpty())
			return;
		var unroller = new WasteScenarios(dataSet);
		var params = unroller.unroll();
		if (params.isEmpty())
			return;
		dataSet.databaseInputParameters().addAll(params);
	}

	private List<InputParameterRow> unroll() {
		if (dataSet.processes().isEmpty() || dataSet.productStages().isEmpty())
			return Collections.emptyList();

		// collect waste scenarios and materials
		var scenarios = new ArrayList<ProcessBlock>();
		var materials = new HashMap<String, ProductOutputRow>();
		for (var block : dataSet.processes()) {
			if (block.category() == ProcessCategory.WASTE_SCENARIO
				&& block.wasteScenario() != null) {
				scenarios.add(block);
			} else if (block.category() == ProcessCategory.MATERIAL) {
				for (var product : block.products()) {
					materials.put(product.name(), product);
				}
			}
		}
		if (scenarios.isEmpty() || materials.isEmpty())
			return Collections.emptyList();

		var params = new ArrayList<Pair<ProcessBlock, InputParameterRow>>();
		for (var scenario : scenarios) {
			var scenarioName = Processes.nameOf(scenario);
			if (Strings.nullOrEmpty(scenarioName))
				continue;
			var parameter = parameterOf(scenarioName);
			var paramRow = new InputParameterRow()
				.name(parameter)
				.value(0)
				.comment("A switch parameter for the waste scenario \n  ''" +
					scenarioName + "''.\n" +
					"Set the value to 1 to apply the scenario in calculations.\n" +
					"You better do that in parameter redefinitions of product\n" +
					"systems instead of setting the value globally. Also note,\n" +
					"that only one waste scenario should be activated per \n" +
					"calculation.");
			params.add(Pair.of(scenario, paramRow));
		}

		for (var stage : dataSet.productStages()) {
			var materialDefs = stage.materialsAndAssemblies()
				.stream()
				.map(row -> Pair.of(row, materials.get(row.name())))
				.filter(p -> p.second != null)
				.collect(Collectors.toList());
			if (materialDefs.isEmpty() || stage.products().isEmpty())
				continue;

			var block = new ProcessBlock();
			block.name("Unrolled waste scenarios of '"
				+ stage.products().get(0).name() + "'");
			block.category(ProcessCategory.PROCESSING);

			for (var def : materialDefs) {
				var input = def.first;
				var material = def.second;
				block.products().add(new ProductOutputRow()
					.name(material.name())
					.category(material.category())
					.wasteType(material.wasteType())
					.amount(input.amount())
					.unit(input.unit()));
			}
			for (var param : params) {
				apply(param.second.name(), param.first, block);
			}
			block.products().clear();
			block.products().add(new ProductOutputRow()
				.name(block.name())
				.category("Unrolled waste scenarios")
				.amount(Numeric.of(1))
				.unit("p"));
			dataSet.processes().add(block);

			stage.processes().add(new TechExchangeRow()
				.name(block.name())
				.amount(Numeric.of(1))
				.unit("p"));
		}


		return params.stream()
			.map(p -> p.second)
			.collect(Collectors.toList());
	}

	private void apply(String param, ProcessBlock scenario, ProcessBlock block) {
		for (var material : block.products()) {

			// add a waste-to-treatment-row for the scenario to capture the
			// waste handling in the scenario (material, energy, etc. inputs
			// of the scenario)
			var scenarioRow = scenario.wasteScenario();
			block.wasteToTreatment().add(new TechExchangeRow()
				.name(scenarioRow.name())
				.unit(material.unit())
				.amount(combine(param, material.amount()))
				.comment("unrolled scenario contribution for material: "
					+ material.name()));

			// separated fractions
			double separatedTotal = 0.0;
			var separated = getFractions(material, scenario.separatedWaste());
			for (var sep : separated) {
				double f = sep.fraction() / 100.0;
				separatedTotal += f;
				var prefix = param + " * " + f;
				block.wasteToTreatment().add(new TechExchangeRow()
					.name(sep.wasteTreatment())
					.unit(material.unit())
					.amount(combine(prefix, material.amount()))
					.comment("unrolled separated fraction for material: "
						+ material.name()));
			}

			// remaining fractions
			double remainingTotal = 1.0 - separatedTotal;
			if (remainingTotal < 1e-9)
				continue;
			var remaining = getFractions(material, scenario.remainingWaste());
			for (var rem : remaining) {
				double f = rem.fraction() / 100.0;
				var prefix = param + " * " + remainingTotal + " * " + f;
				block.wasteToTreatment().add(new TechExchangeRow()
					.name(rem.wasteTreatment())
					.unit(material.unit())
					.amount(combine(prefix, material.amount()))
					.comment("unrolled remaining fraction for material: "
						+ material.name()));
			}
		}
	}

	/**
	 * Get the fractions for the given product with the best matching waste type.
	 */
	private List<WasteFractionRow> getFractions(
		ProductOutputRow material, Collection<WasteFractionRow> fractions) {

		if (fractions.isEmpty())
			return Collections.emptyList();

		// fractions for the specific material
		var forMaterial = fractions.stream()
			.filter(f -> Strings.nullOrEqual(material.name(), f.wasteType()))
			.collect(Collectors.toList());
		if (!forMaterial.isEmpty())
			return forMaterial;

		// fractions for a specific waste type
		if (!matchesAll(material.wasteType())) {
			var forWasteType = fractions.stream()
				.filter(f -> Strings.nullOrEqual(material.wasteType(), f.wasteType()))
				.collect(Collectors.toList());
			if (!forWasteType.isEmpty())
				return forWasteType;
		}

		// finally, the 'matches all' fractions
		return fractions.stream()
			.filter(f -> matchesAll(f.wasteType()))
			.collect(Collectors.toList());
	}

	private Numeric combine(String prefix, Numeric numeric) {
		if (numeric == null)
			return Numeric.zero();
		var s = numeric.hasFormula()
			? numeric.formula()
			: Double.toString(numeric.value());
		return Numeric.of(prefix + " * (" + s + ")");
	}

	static String parameterOf(String scenarioName) {
		var buffer = new StringBuilder(PARAMETER_PREFIX);
		boolean wasValid = true;
		for (char c : scenarioName.toLowerCase().toCharArray()) {
			if (Character.isJavaIdentifierPart(c)) {
				wasValid = true;
				buffer.append(c);
				continue;
			}
			if (wasValid) {
				buffer.append('_');
			}
			wasValid = false;
		}
		return buffer.toString();
	}

	/**
	 * Returns true if the given waste type matches all other waste types. This
	 * means, a treatment of a fraction tagged with this type can handle all
	 * materials. On the other side, a material tagged with this type has no
	 * specific waste type and cannot be separated by fractions tagged with a
	 * specific waste type.
	 */
	private boolean matchesAll(String wasteType) {
		if (Strings.nullOrEmpty(wasteType))
			return true;
		var wt = wasteType.strip().toLowerCase();
		return wt.isEmpty()
			|| wt.equals("not defined")
			|| wt.equals("unspecified")
			|| wt.equals("all waste types");
	}
}
