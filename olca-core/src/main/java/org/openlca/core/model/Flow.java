package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

	public static Flow product(String name, FlowProperty property) {
		return of(name, FlowType.PRODUCT_FLOW, property);
	}

	public static Flow waste(String name, FlowProperty property) {
		return of(name, FlowType.WASTE_FLOW, property);
	}

	public static Flow elementary(String name, FlowProperty property) {
		return of(name, FlowType.ELEMENTARY_FLOW, property);
	}

	public static Flow of(String name, FlowType type, FlowProperty property) {
		var flow = new Flow();
		flow.name = name;
		flow.refId = UUID.randomUUID().toString();
		flow.flowType = type;
		flow.referenceFlowProperty = property;
		var factor = new FlowPropertyFactor();
		factor.conversionFactor = 1.0;
		factor.flowProperty = property;
		flow.flowPropertyFactors.add(factor);
		return flow;
	}

	@Override
	public Flow clone() {
		var clone = new Flow();
		Util.copyFields(this, clone);
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
	 * Adds a conversion factor with the given flow property to this flow.
	 */
	public FlowPropertyFactor property(FlowProperty prop, double factor) {
		FlowPropertyFactor f = new FlowPropertyFactor();
		f.flowProperty = prop;
		f.conversionFactor = factor;
		flowPropertyFactors.add(f);
		return f;
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

	/**
	 * Get the conversion factor for the given flow property from this flow.
	 */
	public FlowPropertyFactor getFactor(FlowProperty property) {
		for (FlowPropertyFactor f : flowPropertyFactors) {
			if (Objects.equals(f.flowProperty, property)) {
				return f;
			}
		}
		return null;
	}

}
