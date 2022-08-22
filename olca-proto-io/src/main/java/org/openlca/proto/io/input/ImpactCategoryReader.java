
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Direction;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.proto.ProtoImpactCategory;
import org.openlca.proto.ProtoImpactFactor;

public record ImpactCategoryReader(EntityResolver resolver)
	implements EntityReader<ImpactCategory, ProtoImpactCategory> {

	@Override
	public ImpactCategory read(ProtoImpactCategory proto) {
		var impact = new ImpactCategory();
		update(impact, proto);
		return impact;
	}

	@Override
	public void update(ImpactCategory impact, ProtoImpactCategory proto) {
		Util.mapBase(impact, ProtoBox.of(proto), resolver);

		impact.referenceUnit = proto.getRefUnit();
		impact.direction = switch (proto.getDirection()) {
			case INPUT -> Direction.INPUT;
			case OUTPUT -> Direction.OUTPUT;
			default -> null;
		};
		impact.code = proto.getCode();
		impact.source = Util.getSource(resolver, proto.getSource());
		mapParameters(impact, proto);

		// impact factors
		impact.impactFactors.clear();
		for (int i = 0; i < proto.getImpactFactorsCount(); i++) {
			var protoFactor = proto.getImpactFactors(i);
			var factor = mapFactor(protoFactor);
			if (factor == null)
				continue;
			impact.impactFactors.add(factor);
		}
	}

	private void mapParameters(ImpactCategory impact, ProtoImpactCategory proto) {
		impact.parameters.clear();
		for (int i = 0; i < proto.getParametersCount(); i++) {
			var protoParam = proto.getParameters(i);
			var parameter = new Parameter();
			ParameterReader.mapFields(parameter, protoParam, resolver);
			parameter.scope = ParameterScope.IMPACT;
			impact.parameters.add(parameter);
		}
	}

	private ImpactFactor mapFactor(ProtoImpactFactor proto) {

		var factor = new ImpactFactor();

		// flow
		var flow = Util.getFlow(resolver, proto.getFlow());
		factor.flow = flow;
		if (flow == null) {
			return null;
		}

		// unit & flow property
		var quantity = Quantity.of(flow)
			.withProperty(proto.getFlowProperty())
			.withUnit(proto.getUnit())
			.get();
		factor.unit = quantity.unit();
		factor.flowPropertyFactor = quantity.factor();

		// amount fields
		factor.value = proto.getValue();
		factor.formula = proto.getFormula();
		factor.uncertainty = Util.uncertaintyOf(proto.getUncertainty());

		// location
		factor.location = Util.getLocation(resolver, proto.getLocation());

		return factor;
	}
}
