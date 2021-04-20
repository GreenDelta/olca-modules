package org.openlca.proto.output;

import org.openlca.core.model.Source;
import org.openlca.proto.generated.EntityType;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

public class SourceWriter {

  private final WriterConfig config;

  public SourceWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.Source write(Source source) {
    var proto = Proto.Source.newBuilder();
    if (source == null)
      return proto.build();
    proto.setEntityType(EntityType.Source);
    Out.map(source, proto);
    Out.dep(config, source.category);

    // model specific fields
    proto.setExternalFile(Strings.orEmpty(source.externalFile));
    proto.setTextReference(Strings.orEmpty(source.textReference));
    proto.setUrl(Strings.orEmpty(source.url));
    if (source.year != null) {
      proto.setYear(source.year);
    }
    return proto.build();
  }
}
