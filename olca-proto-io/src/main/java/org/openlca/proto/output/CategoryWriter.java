package org.openlca.proto.output;

import org.openlca.core.model.Category;
import org.openlca.proto.generated.EntityType;
import org.openlca.proto.generated.Proto;

public class CategoryWriter {

  private final WriterConfig config;

  public CategoryWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.Category write(Category c) {
    var proto = Proto.Category.newBuilder();
    if (c == null)
      return proto.build();
    proto.setEntityType(EntityType.Category);
    Out.map(c, proto);
    Out.dep(config, c.category);
    proto.setModelType(Out.modelTypeOf(c.modelType));
    return proto.build();
  }
}
