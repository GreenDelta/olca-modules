package org.openlca.proto.output;

import java.util.Arrays;

import org.openlca.core.model.Category;
import org.openlca.proto.generated.Proto;
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
    Out.map(c, proto);
    Out.dep(config, c.category);
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
