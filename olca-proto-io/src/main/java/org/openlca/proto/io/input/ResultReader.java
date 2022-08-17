
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Result;
import org.openlca.proto.ProtoResult;

public record ResultReader(EntityResolver resolver)
	implements EntityReader<Result, ProtoResult> {

	@Override
	public Result read(ProtoResult proto) {
		var result = new Result();
		update(result, proto);
		return result;
	}

	@Override
	public void update(Result result, ProtoResult proto) {
		Util.mapBase(result, ProtoWrap.of(proto), resolver);

	}
}
