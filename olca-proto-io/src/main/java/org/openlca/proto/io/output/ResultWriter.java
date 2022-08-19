package org.openlca.proto.io.output;

import org.openlca.core.model.Result;
import org.openlca.proto.ProtoResult;
import org.openlca.proto.ProtoType;

public class ResultWriter {

	private final WriterConfig config;

	public ResultWriter(WriterConfig config) {
		this.config = config;
	}

	public ProtoResult write(Result result) {
		var proto = ProtoResult.newBuilder();
		if (result == null)
			return proto.build();
		proto.setType(ProtoType.Result);
		Out.map(result, proto);

		// TODO: set result fields

		return proto.build();
	}

}
