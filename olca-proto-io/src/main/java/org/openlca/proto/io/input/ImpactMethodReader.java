
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ImpactMethod;
import org.openlca.proto.ProtoImpactMethod;

public record ImpactMethodReader(EntityResolver resolver)
	implements EntityReader<ImpactMethod, ProtoImpactMethod> {

	@Override
	public ImpactMethod read(ProtoImpactMethod proto) {
		var method = new ImpactMethod();
		update(method, proto);
		return method;
	}

	@Override
	public void update(ImpactMethod method, ProtoImpactMethod proto) {
		Util.mapBase(method, ProtoWrap.of(proto), resolver);

	}
}
