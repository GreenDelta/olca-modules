
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Process;
import org.openlca.proto.ProtoProcess;

public record ProcessReader(EntityResolver resolver)
	implements EntityReader<Process, ProtoProcess> {

	@Override
	public Process read(ProtoProcess proto) {
		var process = new Process();
		update(process, proto);
		return process;
	}

	@Override
	public void update(Process process, ProtoProcess proto) {
		Util.mapBase(process, ProtoWrap.of(proto), resolver);

	}
}
