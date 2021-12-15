package org.openlca.io.maps;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Copyable;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Strings;

/**
 * A FlowRef contains the associated reference data of a source or target flow
 * in a flow mapping.
 */
public class FlowRef implements Copyable<FlowRef> {

	/**
	 * The reference information of a flow data set. This information is
	 * required and at least the field `flow.refId` must be present.
	 */
	public FlowDescriptor flow;

	/**
	 * An optional category path of the flow.
	 */
	public String flowCategory;

	/**
	 * An optional location code. of the flow.
	 */
	public String flowLocation;

	/**
	 * An optional reference to a property (= quantity) of the flow. When this
	 * is missing, the reference flow property of the flow is taken by default.
	 */
	public Descriptor property;

	/**
	 * Also, the unit reference is optional; the reference unit of the unit
	 * group of the flow property is taken by default.
	 */
	public Descriptor unit;

	/**
	 * An optional reference to a provider process in case this reference
	 * describes a (target) product or waste flow.
	 */
	public ProcessDescriptor provider;

	/**
	 * An location code of the provider process.
	 */
	public String providerLocation;

	/**
	 * An optional category path of the provider process.
	 */
	public String providerCategory;

	/**
	 * Describes a synchronization result of this flow mapping with a data
	 * source.
	 */
	public MappingStatus status;

	/**
	 * Creates an unique identifier of this flow reference which is a
	 * concatenation of the UUIDs of the referenced entities.
	 */
	public String key() {
		var ids = new String[4];
		if (flow != null) {
			ids[0] = flow.refId;
		}
		if (property != null) {
			ids[1] = property.refId;
		}
		if (unit != null) {
			ids[2] = unit.refId;
		}
		if (provider != null) {
			ids[3] = provider.refId;
		}
		return String.join("/", ids);
	}

	@Override
	public FlowRef copy() {
		var clone = new FlowRef();
		clone.flow = flow != null
			? flow.copy()
			: null;
		clone.flowCategory = flowCategory;
		clone.flowLocation = flowLocation;
		clone.property = property != null
			? property.copy()
			: null;
		clone.unit = unit != null
			? unit.copy()
			: null;
		clone.provider = provider != null
			? provider.copy()
			: null;
		clone.providerLocation = providerLocation;
		clone.providerCategory = providerCategory;
		clone.status = status != null
			? status.copy()
			: null;
		return clone;
	}

	/**
	 * Tries to find a matching flow from the given database. It checks
	 * the reference ID of the flows and also of the flow property and
	 * unit if these are present.
	 */
	public Flow getMatchingFlow(IDatabase db) {
		if (db == null
				|| flow == null
				|| flow.refId == null)
			return null;

		var f = db.get(Flow.class, flow.refId);
		if (f == null)
			return null;
		return getMatchingUnit(f) != null
				? f
				: null;
	}

	/**
	 * Get the matching flow property from the given flow, which is
	 * the reference flow property of that flow if not specified
	 * otherwise. Note that this can be null, if there is no
	 * matching flow property defined in the given flow.
	 */
	public FlowPropertyFactor getMatchingProperty(Flow flow) {
		if (flow == null)
			return null;
		if (property == null || Strings.nullOrEmpty(property.refId))
			return flow.getReferenceFactor();
		for (var f : flow.flowPropertyFactors) {
			if (f == null || f.flowProperty == null)
				continue;
			if (Strings.nullOrEqual(
					f.flowProperty.refId, property.refId))
				return f;
		}
		return null;
	}

	/**
	 * Get the matching unit from the given flow, which is the
	 * reference unit of that flow if not specified otherwise.
	 * Note that this can be null, if there is no matching
	 * unit defined in the given flow.
	 */
	public Unit getMatchingUnit(Flow flow) {
		var fac = getMatchingProperty(flow);
		if (fac == null
				|| fac.flowProperty == null
				|| fac.flowProperty.unitGroup == null)
			return null;
		var group = fac.flowProperty.unitGroup;
		if (unit == null || Strings.nullOrEmpty(unit.refId))
			return group.referenceUnit;
		return group.units.stream()
				.filter(u -> Strings.nullOrEqual(u.refId, unit.refId))
				.findAny()
				.orElse(null);
	}

}
