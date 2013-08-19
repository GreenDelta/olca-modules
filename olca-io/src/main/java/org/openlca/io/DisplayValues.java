package org.openlca.io;

import org.openlca.core.database.Cache;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that maps openLCA models to display values.
 */
public final class DisplayValues {

	private static Logger log = LoggerFactory.getLogger(DisplayValues.class);

	private DisplayValues() {
	}

	/** Returns the name of the reference unit of the given flow. */
	public static String referenceUnit(FlowDescriptor flow, Cache cache) {
		FlowPropertyDescriptor descriptor = cache
				.getFlowPropertyDescriptor(flow.getRefFlowPropertyId());
		if (descriptor == null) {
			log.warn("no reference flow property for flow {}", flow);
			return "";
		}
		try {
			FlowProperty property = cache.getFlowProperty(descriptor.getId());
			Unit refUnit = property.getUnitGroup().getReferenceUnit();
			return refUnit.getName();
		} catch (Exception e) {
			log.error("failed to get reference unit for flow " + flow.getId(),
					e);
			return "";
		}
	}

}
