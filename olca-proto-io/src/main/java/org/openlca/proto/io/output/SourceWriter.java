package org.openlca.proto.io.output;

import org.openlca.core.model.Source;
import org.openlca.proto.ProtoSource;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class SourceWriter {

  public ProtoSource write(Source source) {
    var proto = ProtoSource.newBuilder();
    if (source == null)
      return proto.build();
    proto.setType(ProtoType.Source);
    Out.map(source, proto);

    proto.setExternalFile(Strings.orEmpty(source.externalFile));
    proto.setTextReference(Strings.orEmpty(source.textReference));
    proto.setUrl(Strings.orEmpty(source.url));
    if (source.year != null) {
      proto.setYear(source.year);
    }
    return proto.build();
  }
}
