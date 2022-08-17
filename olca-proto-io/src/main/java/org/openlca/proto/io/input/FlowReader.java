
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Flow;
import org.openlca.proto.ProtoFlow;

public record FlowReader(EntityResolver resolver)
	implements EntityReader<Flow, ProtoFlow> {

	@Override
	public Flow read(ProtoFlow proto) {
		var flow = new Flow();
		update(flow, proto);
		return flow;
	}

	@Override
	public void update(Flow flow, ProtoFlow proto) {
		Util.mapBase(flow, ProtoWrap.of(proto), resolver);

	}
}
