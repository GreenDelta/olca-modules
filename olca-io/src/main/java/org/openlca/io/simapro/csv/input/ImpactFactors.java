package org.openlca.io.simapro.csv.input;

import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.SubCompartment;
import org.openlca.simapro.csv.method.ImpactCategoryBlock;
import org.openlca.simapro.csv.method.ImpactFactorRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class ImpactFactors {

	private ImpactFactors() {}

	public static void expand(ImpactCategoryBlock impactCategory) {
		var expandedFactors = new ArrayList<ImpactFactorRow>();
		impactCategory.factors().stream()
			.filter(factor -> factor.subCompartment().equals("(unspecified)"))
			.forEach(factor -> {
				var subCompartments = subCompartmentsOf(
					factor.flow(),
					impactCategory
				);
				subCompartmentsOf(ElementaryFlowType.of(factor.compartment()))
					.filter(subCompartment -> !subCompartments.contains(subCompartment))
					.forEach(subCompartment -> expandedFactors.add(
						new ImpactFactorRow()
							.compartment(factor.compartment())
							.subCompartment(subCompartment.toString())
							.flow(factor.flow())
							.casNumber(factor.casNumber())
							.factor(factor.factor())
							.unit(factor.unit())
					));
			});
		impactCategory.factors().addAll(expandedFactors);
	}

	private static Set<SubCompartment> subCompartmentsOf(String flow, ImpactCategoryBlock impactCategory) {
		var subCompartments = new HashSet<SubCompartment>();
		impactCategory.factors().stream()
			.filter(factor -> factor.flow().equals(flow))
			.forEach(factor -> subCompartments.add(SubCompartment.of(factor.subCompartment())));
		return subCompartments;
	}

	private static Stream<SubCompartment> subCompartmentsOf(ElementaryFlowType type) {
		return Arrays.stream(SubCompartment.values())
			.filter(sc -> sc.flowType() != null)
			.filter(sc -> sc.flowType().equals(type));
	}
}
