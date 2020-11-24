package org.openlca.io.maps;

import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;

/**
 * Describes an openLCA flow in a mapping list. See the reference data
 * definition for a detailed description of the fields (REF_DATA.md).
 */
public class OlcaFlowMapEntry {

	public String flowId;
	public String refPropertyId;
	public String refUnitId;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		var other = (OlcaFlowMapEntry) obj;
		return Objects.equals(this.flowId, other.flowId)
				&& Objects.equals(this.refPropertyId, other.refPropertyId)
				&& Objects.equals(this.refUnitId, other.refUnitId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(flowId, refPropertyId, refUnitId);
	}

	/**
	 * Loads the matching flow from the database if there is any; returns
	 * null otherwise. The result of this method should be cached as searching
	 * the database by reference IDs is quite expensive.
	 */
	public Flow getMatchingFlow(IDatabase db) throws Exception {
		if (flowId == null)
			return null;
		var flow  = db.get(Flow.class, flowId);
		return matches(flow)
				? flow
				: null;
	}

	/**
	 * Returns true if the given flow matches the specification of this mapping
	 * entry.
	 */
	public boolean matches(Flow flow) {
		if (flow == null || flow.referenceFlowProperty == null)
			return false;
		FlowProperty property = flow.referenceFlowProperty;
		if (property.unitGroup == null
				|| property.unitGroup.referenceUnit == null)
			return false;
		Unit unit = property.unitGroup.referenceUnit;
		return Objects.equals(this.flowId, flow.refId)
				&& Objects.equals(this.refPropertyId, property.refId)
				&& Objects.equals(this.refUnitId, unit.refId);
	}
}
