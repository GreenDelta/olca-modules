package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.Category;
import org.openlca.core.model.Version;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class CategoryWriter {

  private final WriterConfig config;

  public CategoryWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.Category write(Category c) {
    var proto = Proto.Category.newBuilder();
    if (c == null)
      return proto.build();

    // root entity fields
    proto.setType("Category");
    proto.setId(Strings.orEmpty(c.refId));
    proto.setName(Strings.orEmpty(c.name));
    proto.setDescription(Strings.orEmpty(c.description));
    proto.setVersion(Version.asString(c.version));
    if (c.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(c.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(c.tags)) {
      Arrays.stream(c.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (c.category != null) {
      proto.setCategory(Out.refOf(c.category, config));
    }

    proto.setModelType(type(c));

    return proto.build();
  }

  private Proto.ModelType type(Category c) {
    if (c == null || c.modelType == null)
      return Proto.ModelType.UNDEFINED_MODEL_TYPE;
    var name = c.modelType.name();
    return Arrays.stream(Proto.ModelType.values())
      .filter(t -> Strings.nullOrEqual(t.name(), name))
      .findAny()
      .orElse(Proto.ModelType.UNDEFINED_MODEL_TYPE);
  }
}
