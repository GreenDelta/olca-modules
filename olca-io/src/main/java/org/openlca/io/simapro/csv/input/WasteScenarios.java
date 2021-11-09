package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
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
import org.openlca.util.Strings;

class WasteScenarios {

	private final CsvDataSet dataSet;
	private final EnumSet<ProcessCategory> applicable = EnumSet.of(
		ProcessCategory.MATERIAL,
		ProcessCategory.ENERGY,
		ProcessCategory.PROCESSING,
		ProcessCategory.TRANSPORT,
		ProcessCategory.USE
	);

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
		if (dataSet.processes().isEmpty())
			return Collections.emptyList();
		var scenarios = dataSet.processes()
			.stream()
			.filter(block -> block.category() == ProcessCategory.WASTE_SCENARIO)
			.collect(Collectors.toList());
		if (scenarios.isEmpty())
			return Collections.emptyList();

		var params = new ArrayList<InputParameterRow>();
		for (var scenario : scenarios) {
			var scenarioName = Processes.nameOf(scenario);
			if (Strings.nullOrEmpty(scenarioName))
				continue;
			var parameter = parameterOf(scenarioName);
			params.add(new InputParameterRow()
				.name(parameter)
				.value(0)
				.comment("A switch parameter for the waste scenario \n  ''" +
					scenarioName + "''.\n" +
					"Set the value to 1 to apply the scenario in calculations.\n" +
					"You better do that in parameter redefinitions of product\n" +
					"systems instead of setting the value globally. Also note,\n" +
					"that only one waste scenario should be activated per \n" +
					"calculation."));
			for (var block : dataSet.processes()) {
				apply(parameter, scenario, block);
			}
		}
		return params;
	}

	private void apply(String param, ProcessBlock scenario, ProcessBlock block) {
		if (block.category() == null || !applicable.contains(block.category()))
			return;
		for (var product : block.products()) {
			if (!canApply(scenario, product))
				continue;

			// add a waste-to-treatment-row for the scenario to capture the
			// waste handling in the scenario (material, energy, etc. inputs
			// of the scenario)
			var scenarioRow = scenario.wasteScenario();
			block.wasteToTreatment().add(new TechExchangeRow()
				.name(scenarioRow.name())
				.unit(product.unit())
				.amount(combine(param, product.amount())));

			// separated fractions
			double separatedTotal = 0.0;
			var separated = getFractions(product, scenario.separatedWaste());
			for (var sep : separated) {
				double f = sep.fraction() / 100.0;
				separatedTotal += f;
				var prefix = param + " * " + f;
				block.wasteToTreatment().add(new TechExchangeRow()
					.name(sep.wasteTreatment())
					.unit(product.unit())
					.amount(combine(prefix, product.amount())));
			}

			// remaining fractions
			double remainingTotal = 1.0 - separatedTotal;
			if (remainingTotal < 0.01)
				continue;
			var remaining = getFractions(product, scenario.remainingWaste());
			for (var rem : remaining) {
				double f = rem.fraction() / 100.0;
				var prefix = param + " * " + remainingTotal + " * " + f;
				block.wasteToTreatment().add(new TechExchangeRow()
					.name(rem.wasteTreatment())
					.unit(product.unit())
					.amount(combine(prefix, product.amount())));
			}
		}
	}

	private List<WasteFractionRow> getFractions(
		ProductOutputRow product, Collection<WasteFractionRow> fractions) {
		var matchedType = findMatchingType(product.wasteType(), fractions);
		if (matchedType == null)
			return Collections.emptyList();
		return fractions.stream()
			.filter(row -> matchedType.equals(row.wasteType()))
			.collect(Collectors.toList());
	}

	private boolean canApply(ProcessBlock scenario, ProductOutputRow product) {
		var productWasteType = product.wasteType();
		if (Strings.nullOrEmpty(productWasteType)
			|| productWasteType.equals("not defined"))
			return false;

		var scenarioWasteType = scenario.wasteScenario() != null
			? scenario.wasteScenario().wasteType()
			: null;
		if (Strings.nullOrEmpty(scenarioWasteType))
			return false;

		return productWasteType.equals(scenarioWasteType)
			|| scenarioWasteType.equals("All waste types");
	}

	private String findMatchingType(
		String wasteType, Collection<WasteFractionRow> fractions) {
		String matchedType = null;
		for (var fraction : fractions) {
			var fractionType = fraction.wasteType();
			if (fractionType == null)
				continue;
			if (fractionType.equals(wasteType)) {
				return fractionType;
			}
			if (fractionType.equals("All waste types")
				|| fractionType.isBlank()) {
				matchedType = fractionType;
			}
		}
		return matchedType;
	}

	private Numeric combine(String prefix, Numeric numeric) {
		if (numeric == null)
			return Numeric.zero();
		var s = numeric.hasFormula()
			? numeric.formula()
			: Double.toString(numeric.value());
		return Numeric.of(prefix + " * (" + s + ")");
	}

	private String parameterOf(String scenarioName) {
		var buffer = new StringBuilder("ws_");
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
}
