package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_results")
public class Result extends RootEntity {

	@OneToOne
	@JoinColumn(name = "f_product_system")
	public ProductSystem productSystem;

	@OneToOne
	@JoinColumn(name = "f_impact_method")
	public ImpactMethod impactMethod;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_result")
	public final List<FlowResult> flowResults = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_result")
	public final List<ImpactResult> impactResults = new ArrayList<>();

	/**
	 * The reference flow or quantitative reference of this result. This can be a
	 * product output or waste input. With this flow, a result can be linked as a
	 * provider in a product system to product inputs or waste outputs of other
	 * processes.
	 */
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_reference_flow")
	public FlowResult referenceFlow;

	public static Result of(String name) {
		return of(name, null);
	}

	public static Result of(String name, Flow refFlow) {
		var result = new Result();
		Entities.init(result, name);
		if (refFlow != null) {
			var qRef = refFlow.flowType == FlowType.WASTE_FLOW
				? FlowResult.inputOf(refFlow, 1)
				: FlowResult.outputOf(refFlow, 1);
			result.referenceFlow = qRef;
			result.flowResults.add(qRef);
		}
		return result;
	}

	@Override
	public Result copy() {
		var copy = new Result();
		Entities.copyRefFields(this, copy);
		copy.productSystem = productSystem;
		copy.impactMethod = impactMethod;
		if (referenceFlow != null) {
			copy.referenceFlow = referenceFlow.copy();
		}
		for (var flow : flowResults) {
			copy.flowResults.add(flow.copy());
		}
		for (var impact : impactResults) {
			copy.impactResults.add(impact.copy());
		}
		return copy;
	}
}
