package org.openlca.io.maps;

import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A FlowRef contains the associated reference data of a source or target flow
 * in a flow mapping.
 */
public class FlowRef {

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
	public Status status;

	/**
	 * Creates an unique identifier of this flow reference which is a
	 * concatenation of the UUIDs of the referenced entities.
	 */
	public String key() {
		String[] ids = new String[4];
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
	public FlowRef clone() {
		FlowRef clone = new FlowRef();
		clone.flow = copy(flow);
		clone.flowCategory = flowCategory;
		clone.flowLocation = flowLocation;
		clone.property = copy(property);
		clone.unit = copy(unit);
		clone.provider = copy(provider);
		clone.providerLocation = providerLocation;
		clone.providerCategory = providerCategory;
		if (status != null) {
			clone.status = status.clone();
		}
		return clone;
	}

	@SuppressWarnings("unchecked")
	private <T extends Descriptor> T copy(T d) {
		if (d == null)
			return null;
		try {
			T clone = (T) d.getClass().newInstance();
			clone.description = d.description;
			clone.id = d.id;
			clone.lastChange = d.lastChange;
			clone.name = d.name;
			clone.refId = d.refId;
			clone.type = d.type;
			clone.version = d.version;

			if (d instanceof FlowDescriptor) {
				FlowDescriptor fd = (FlowDescriptor) d;
				FlowDescriptor fclone = (FlowDescriptor) clone;
				fclone.category = fd.category;
				fclone.flowType = fd.flowType;
				fclone.location = fd.location;
				fclone.refFlowPropertyId = fd.refFlowPropertyId;
			}

			if (d instanceof ProcessDescriptor) {
				ProcessDescriptor pd = (ProcessDescriptor) d;
				ProcessDescriptor pclone = (ProcessDescriptor) clone;
				pclone.category = pd.category;
				pclone.infrastructureProcess = pd.infrastructureProcess;
				pclone.location = pd.location;
				pclone.processType = pd.processType;
				pclone.quantitativeReference = pd.quantitativeReference;
			}

			return clone;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
