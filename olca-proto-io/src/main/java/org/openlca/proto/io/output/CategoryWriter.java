package org.openlca.proto.io.output;

import org.openlca.core.model.Category;
import org.openlca.proto.ProtoCategory;
import org.openlca.proto.ProtoCategoryType;
import org.openlca.proto.ProtoType;

public class CategoryWriter {

  private final WriterConfig config;

  public CategoryWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoCategory write(Category c) {
    var proto = ProtoCategory.newBuilder();
    if (c == null)
      return proto.build();
    proto.setType(ProtoType.Category);
    Out.map(c, proto);
    Out.dep(config, c.category);
    proto.setModelType(categoryTypeOf(c));
    return proto.build();
  }

  private ProtoCategoryType categoryTypeOf(Category c) {
    if (c.modelType == null)
      return ProtoCategoryType.UNDEFINED_CATEGORY_TYPE;
    return switch (c.modelType) {
      case ACTOR -> ProtoCategoryType.ACTOR;
      case CURRENCY -> ProtoCategoryType.CURRENCY;
      case DQ_SYSTEM -> ProtoCategoryType.DQ_SYSTEM;
      case FLOW -> ProtoCategoryType.FLOW;
      case FLOW_PROPERTY -> ProtoCategoryType.FLOW_PROPERTY;
      case IMPACT_CATEGORY -> ProtoCategoryType.IMPACT_CATEGORY;
      case IMPACT_METHOD -> ProtoCategoryType.IMPACT_METHOD;
      case LOCATION -> ProtoCategoryType.LOCATION;
      case PARAMETER -> ProtoCategoryType.PARAMETER;
      case PROCESS -> ProtoCategoryType.PROCESS;
      case PRODUCT_SYSTEM -> ProtoCategoryType.PRODUCT_SYSTEM;
      case PROJECT -> ProtoCategoryType.PROJECT;
      case RESULT -> ProtoCategoryType.RESULT;
      case SOCIAL_INDICATOR -> ProtoCategoryType.SOCIAL_INDICATOR;
      case SOURCE -> ProtoCategoryType.SOURCE;
      case UNIT_GROUP -> ProtoCategoryType.UNIT_GROUP;
      default -> ProtoCategoryType.UNDEFINED_CATEGORY_TYPE;
    };
  }
}
