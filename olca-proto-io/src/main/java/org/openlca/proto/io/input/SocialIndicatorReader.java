
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Unit;
import org.openlca.proto.ProtoSocialIndicator;
import org.openlca.util.Strings;

public record SocialIndicatorReader(EntityResolver resolver)
	implements EntityReader<SocialIndicator, ProtoSocialIndicator> {

	@Override
	public SocialIndicator read(ProtoSocialIndicator proto) {
		var indicator = new SocialIndicator();
		update(indicator, proto);
		return indicator;
	}

	@Override
	public void update(SocialIndicator indicator, ProtoSocialIndicator proto) {
		Util.mapBase(indicator, ProtoWrap.of(proto), resolver);
		indicator.activityVariable = proto.getActivityVariable();
		indicator.evaluationScheme = proto.getEvaluationScheme();
		indicator.unitOfMeasurement = proto.getUnitOfMeasurement();

		// activity quantity and unit
		indicator.activityQuantity = Util.getFlowProperty(
			resolver, proto.getActivityQuantity());
		if (indicator.activityQuantity == null)
			return;
		indicator.activityUnit = findUnit(
			indicator.activityQuantity, proto.getActivityUnit().getId());
	}

	private Unit findUnit(FlowProperty prop, String unitId) {
		if (prop == null || prop.unitGroup == null || unitId == null)
			return null;
		for (var unit : prop.unitGroup.units) {
			if (Strings.nullOrEqual(unit.refId, unitId))
				return unit;
		}
		return null;
	}
}
