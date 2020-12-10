package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.proto.Proto;
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

    // root entity fields
    proto.setType("Source");
    proto.setId(Strings.orEmpty(source.refId));
    proto.setName(Strings.orEmpty(source.name));
    proto.setDescription(Strings.orEmpty(source.description));
    proto.setVersion(Version.asString(source.version));
    if (source.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(source.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(source.tags)) {
      Arrays.stream(source.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (source.category != null) {
      proto.setCategory(Out.refOf(source.category, config));
    }

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
