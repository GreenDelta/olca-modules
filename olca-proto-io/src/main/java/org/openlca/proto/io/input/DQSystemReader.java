
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.DQSystem;
import org.openlca.proto.ProtoDQSystem;

public record DQSystemReader(EntityResolver resolver)
	implements EntityReader<DQSystem, ProtoDQSystem> {

	@Override
	public DQSystem read(ProtoDQSystem proto) {
		var system = new DQSystem();
		update(system, proto);
		return system;
	}

	@Override
	public void update(DQSystem system, ProtoDQSystem proto) {
		Util.mapBase(system, ProtoWrap.of(proto), resolver);

	}
}
