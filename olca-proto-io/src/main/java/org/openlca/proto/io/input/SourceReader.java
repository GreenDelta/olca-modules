
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Source;
import org.openlca.proto.ProtoSource;

public record SourceReader(EntityResolver resolver)
	implements EntityReader<Source, ProtoSource> {

	@Override
	public Source read(ProtoSource proto) {
		var source = new Source();
		update(source, proto);
		return source;
	}

	@Override
	public void update(Source source, ProtoSource proto) {
		Util.mapBase(source, ProtoBox.of(proto), resolver);
		source.url = proto.getUrl();
		source.externalFile = proto.getExternalFile();
		source.textReference = proto.getTextReference();
		if (proto.hasYear()) {
			source.year = (short) proto.getYear();
		}
	}
}
