package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_flows")
public class Flow extends CategorizedEntity {

	@Column(name = "flow_type")
	@Enumerated(EnumType.STRING)
	public FlowType flowType;

	@Column(name = "cas_number")
	public String casNumber;

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_flow")
	public final List<FlowPropertyFactor> flowPropertyFactors = new ArrayList<>();

	@Column(name = "formula")
	public String formula;

	@Column(name = "infrastructure_flow")
	public boolean infrastructureFlow;

	@Column(name = "synonyms")
	public String synonyms;

	@OneToOne
	@JoinColumn(name = "f_location")
	public Location location;

	@OneToOne
	@JoinColumn(name = "f_reference_flow_property")
	public FlowProperty referenceFlowProperty;

	@Override
	public Flow clone() {
		Flow clone = new Flow();
		Util.cloneRootFields(this, clone);
		clone.category = category;
		clone.flowType = flowType;
		clone.casNumber = casNumber;
		clone.formula = formula;
		clone.infrastructureFlow = infrastructureFlow;
		clone.location = location;
		clone.referenceFlowProperty = referenceFlowProperty;
		for (FlowPropertyFactor factor : flowPropertyFactors) {
			clone.flowPropertyFactors.add(factor.clone());
		}
		clone.synonyms = synonyms;
		return clone;
	}

	/**
	 * Get the flow property factor of the reference flow property of this flow.
	 */
	public FlowPropertyFactor getReferenceFactor() {
		if (referenceFlowProperty == null)
			return null;
		for (FlowPropertyFactor f : flowPropertyFactors) {
			if (Objects.equals(referenceFlowProperty, f.flowProperty))
				return f;
		}
		return null;
	}

	/**
	 * Returns the reference unit of this flow. More specifically, it returns the
	 * reference unit of the unit group of the reference flow property of this flow.
	 * In openLCA, results of a flow are always calculated in its reference unit.
	 */
	public Unit getReferenceUnit() {
		if (referenceFlowProperty == null)
			return null;
		if (referenceFlowProperty.unitGroup == null)
			return null;
		return referenceFlowProperty.unitGroup.referenceUnit;
	}

	public FlowPropertyFactor getFactor(FlowProperty property) {
		for (FlowPropertyFactor f : flowPropertyFactors)
			if (Objects.equals(f.flowProperty, property))
				return f;
		return null;
	}

}
