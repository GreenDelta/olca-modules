package org.openlca.proto.io.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.proto.ProtoCategory;
import org.openlca.util.Categories;
import org.openlca.util.Strings;

class CategoryImport implements Import<Category> {

  private final ProtoImport imp;

  CategoryImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<Category> of(String id) {
    var mappedID = imp.mappedCategories.get(id);
    var category = mappedID != null
      ? imp.get(Category.class, mappedID)
      : imp.get(Category.class, id);

    // check if we are in update mode
    var update = false;
    if (category != null) {
      update = imp.shouldUpdate(category);
      if(!update) {
        return ImportStatus.skipped(category);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getCategory(id);
    if (proto == null)
      return category != null
        ? ImportStatus.skipped(category)
        : ImportStatus.error("Could not resolve Category " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(category, wrap))
        return ImportStatus.skipped(category);
    }

    // map the data
    if (category == null) {
      category = new Category();
    }
    wrap.mapTo(category, imp);
    category.modelType = modelTypeOf(proto);

    // update a possible parent; this is a bit complicated
    // because we need to return the managed category from JPA
    var dao = new CategoryDao(imp.db);
    var parent = category.category;
    if (parent == null) {
      category = update
        ? dao.update(category)
        : dao.insert(category);
    } else {
      var refID = Categories.createRefId(category);
      category.refId = refID;
      var existing = parent.childCategories.stream()
        .filter(child -> Strings.nullOrEqual(child.refId, refID))
        .findAny()
        .orElse(null);
      if (existing == null) {
        parent.childCategories.add(category);
      } else {
        updateExisting(existing, category);
      }
      parent = dao.update(parent);
      category = parent.childCategories.stream()
        .filter(child -> Strings.nullOrEqual(child.refId, refID))
        .findAny()
        .orElse(null);
      if (category == null)
        return ImportStatus.error("Failed to update parent Category of " + id);
    }

    if (!Strings.nullOrEqual(id, category.refId)) {
      imp.mappedCategories.put(id, category.refId);
    }
    imp.putHandled(category);
    return update
      ? ImportStatus.updated(category)
      : ImportStatus.created(category);
  }

  private void updateExisting(Category existing, Category category) {
    existing.name = category.name;
    existing.description = category.description;
    existing.version = category.version;
    existing.lastChange = category.lastChange;
    existing.tags = category.tags;
    existing.library = category.library;
    existing.modelType = category.modelType;
  }

  private static ModelType modelTypeOf(ProtoCategory proto) {
    return switch (proto.getModelType()) {
      case ACTOR -> ModelType.ACTOR;
      case CURRENCY -> ModelType.CURRENCY;
      case DQ_SYSTEM -> ModelType.DQ_SYSTEM;
      case FLOW -> ModelType.FLOW;
      case FLOW_PROPERTY -> ModelType.FLOW_PROPERTY;
      case IMPACT_CATEGORY -> ModelType.IMPACT_CATEGORY;
      case IMPACT_METHOD -> ModelType.IMPACT_METHOD;
      case LOCATION -> ModelType.LOCATION;
      case PARAMETER -> ModelType.PARAMETER;
      case PROCESS -> ModelType.PROCESS;
      case PRODUCT_SYSTEM -> ModelType.PRODUCT_SYSTEM;
      case PROJECT -> ModelType.PROJECT;
      case RESULT -> ModelType.RESULT;
      case SOCIAL_INDICATOR -> ModelType.SOCIAL_INDICATOR;
      case SOURCE -> ModelType.SOURCE;
      case UNIT_GROUP -> ModelType.UNIT_GROUP;
      case UNRECOGNIZED, UNDEFINED_CATEGORY_TYPE -> ModelType.UNKNOWN;
    };
  }
}
