
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.proto.ProtoFlowProperty;
import org.openlca.proto.ProtoFlowPropertyType;

public record FlowPropertyReader(EntityResolver resolver)
	implements EntityReader<FlowProperty, ProtoFlowProperty> {

	@Override
	public FlowProperty read(ProtoFlowProperty proto) {
		var property = new FlowProperty();
		update(property, proto);
		return property;
	}

	@Override
	public void update(FlowProperty property, ProtoFlowProperty proto) {
		Util.mapBase(property, ProtoWrap.of(proto), resolver);
		property.flowPropertyType =
			proto.getFlowPropertyType() == ProtoFlowPropertyType.ECONOMIC_QUANTITY
				? FlowPropertyType.ECONOMIC
				: FlowPropertyType.PHYSICAL;
		property.unitGroup = Util.getUnitGroup(resolver, proto.getUnitGroup());
	}
}
