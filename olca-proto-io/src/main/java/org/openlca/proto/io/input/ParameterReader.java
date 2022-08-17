
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Parameter;
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
		Util.mapBase(parameter, ProtoWrap.of(proto), resolver);

	}
}
