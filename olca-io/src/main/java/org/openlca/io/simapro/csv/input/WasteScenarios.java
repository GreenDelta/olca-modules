package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.simapro.csv.Numeric;
import org.openlca.simapro.csv.enums.ProcessCategory;
import org.openlca.simapro.csv.process.ProcessBlock;
import org.openlca.simapro.csv.process.ProductOutputRow;
import org.openlca.simapro.csv.process.TechExchangeRow;
import org.openlca.simapro.csv.process.WasteFractionRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;
import org.openlca.util.Strings;

class WasteScenarios {

  private final List<ProcessBlock> blocks;
  private final EnumSet<ProcessCategory> applicable = EnumSet.of(
    ProcessCategory.MATERIAL,
    ProcessCategory.ENERGY,
    ProcessCategory.PROCESSING,
    ProcessCategory.TRANSPORT,
    ProcessCategory.USE
  );

  private WasteScenarios(List<ProcessBlock> block) {
    this.blocks = block;
  }

  static List<InputParameterRow> unroll(List<ProcessBlock> blocks) {
    return new WasteScenarios(blocks).unroll();
  }

  private List<InputParameterRow> unroll() {
    if (blocks == null || blocks.isEmpty())
      return Collections.emptyList();
    var scenarios = blocks.stream()
      .filter(block -> block.category() == ProcessCategory.WASTE_SCENARIO)
      .collect(Collectors.toList());
    if (scenarios.isEmpty())
      return Collections.emptyList();

    var params = new ArrayList<InputParameterRow>();
    for (var i = 0; i < scenarios.size(); i++) {
      var scenario = scenarios.get(i);
      var param = "waste_scenario_" + (i + 1);
      params.add(new InputParameterRow()
        .name(param)
        .value(0)
        .comment("A switch parameter for the waste scenario \n  ''" +
                 nameOf(scenario) + "''.\n" +
                 "Set the value to 1 to apply the scenario in calculations.\n" +
                 "You better do that in parameter redefinitions of product\n" +
                 "systems instead of setting the value globally. Also note,\n" +
                 "that only one waste scenario should be activated per \n" +
                 "calculation."));
      for (var block : blocks) {
        apply(param, scenario, block);
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
      var separatedTotal = 0;
      var separated = getFractions(product, scenario.separatedWaste());
      for (var sep : separated) {
        var f = sep.fraction() / 100;
        separatedTotal += f;
        var prefix = param + " * " + f;
        block.wasteToTreatment().add(new TechExchangeRow()
          .name(sep.wasteTreatment())
          .unit(product.unit())
          .amount(combine(prefix, product.amount())));
      }

      // remaining fractions
      var remainingTotal = 1 - separatedTotal;
      if (remainingTotal < 0.01)
        continue;
      var remaining = getFractions(product, scenario.remainingWaste());
      for (var rem : remaining) {
        var f = rem.fraction() / 100;
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

	private static String nameOf(ProcessBlock block) {
		if (Strings.notEmpty(block.name()))
			return block.name();
		if (!block.products().isEmpty())
			return block.products().get(0).name();
		if (block.wasteTreatment() != null)
			return block.wasteTreatment().name();
		if (block.wasteScenario() != null)
			return block.wasteScenario().name();
		return "?";
	}
}
