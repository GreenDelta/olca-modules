package org.openlca.io.maps;

import java.util.Objects;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;

/**
 * Describes an openLCA flow in a mapping list. See the reference data
 * definition for a detailed description of the fields (REF_DATA.md).
 */
public class OlcaFlowMapEntry {

	private String flowId;
	private String refPropertyId;
	private String refUnitId;

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public String getRefPropertyId() {
		return refPropertyId;
	}

	public void setRefPropertyId(String refPropertyId) {
		this.refPropertyId = refPropertyId;
	}

	public String getRefUnitId() {
		return refUnitId;
	}

	public void setRefUnitId(String refUnitId) {
		this.refUnitId = refUnitId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		OlcaFlowMapEntry other = (OlcaFlowMapEntry) obj;
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
	public Flow getMatchingFlow(IDatabase database) throws Exception {
		if (flowId == null)
			return null;
		FlowDao flowDao = new FlowDao(database);
		Flow flow = flowDao.getForRefId(flowId);
		if (matches(flow))
			return flow;
		else
			return null;
	}

	/**
	 * Returns true if the given flow matches the specification of this mapping
	 * entry.
	 */
	public boolean matches(Flow flow) {
		if (flow == null || flow.getReferenceFlowProperty() == null)
			return false;
		FlowProperty property = flow.getReferenceFlowProperty();
		if (property.getUnitGroup() == null
				|| property.getUnitGroup().getReferenceUnit() == null)
			return false;
		Unit unit = property.getUnitGroup().getReferenceUnit();
		return Objects.equals(this.flowId, flow.getRefId())
				&& Objects.equals(this.refPropertyId, property.getRefId())
				&& Objects.equals(this.refUnitId, unit.getRefId());
	}
}
