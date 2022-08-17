
package org.openlca.proto.io.input;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.input.Uncertainties;
import org.openlca.proto.ProtoParameter;

public record ParameterReader(EntityResolver resolver)
	implements EntityReader<Parameter, ProtoParameter> {

	@Override
	public Parameter read(ProtoParameter proto) {
		var parameter = new Parameter();
		update(parameter, proto);
		return parameter;
	}

	@Override
	public void update(Parameter parameter, ProtoParameter proto) {
		mapFields(parameter, proto, resolver);
	}

	static void mapFields(Parameter param, ProtoParameter proto,
		EntityResolver resolver) {
		Util.mapBase(param, ProtoWrap.of(proto), resolver);
		param.scope = switch (proto.getParameterScope()) {
			case IMPACT_SCOPE -> ParameterScope.IMPACT;
			case PROCESS_SCOPE -> ParameterScope.PROCESS;
			default -> ParameterScope.GLOBAL;
		};
		param.isInputParameter = proto.getIsInputParameter();
		param.value = proto.getValue();
		param.formula = proto.getFormula();
		param.uncertainty = Util.uncertaintyOf(proto.getUncertainty());
	}
}
