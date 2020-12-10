package org.openlca.proto.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Category;
import org.openlca.util.Categories;
import org.openlca.util.Strings;

public class CategoryImport {

  private final ProtoImport imp;

  public CategoryImport(ProtoImport imp) {
    this.imp = imp;
  }

  public Category of(String id) {
    if (id == null)
      return null;
    var mappedID = imp.mappedCategories.get(id);
    var category = mappedID != null
      ? imp.get(Category.class, mappedID)
      : imp.get(Category.class, id);

    // check if we are in update mode
    var update = false;
    if (category != null) {
      update = imp.shouldUpdate(category);
      if(!update) {
        return category;
      }
    }

    var proto = imp.store.getCategory(id);
    if (proto == null)
      return category;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(category, wrap))
        return category;
    }

    if (category == null) {
      category = new Category();
    }
    wrap.mapTo(category, imp);
    category.modelType = In.modelTypeOf(proto.getModelType());

    // update a possible parent
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
        existing.name = category.name;
        existing.description = category.description;
        existing.version = category.version;
        existing.lastChange = category.lastChange;
        existing.tags = category.tags;
        existing.library = category.library;
        existing.modelType = category.modelType;
      }
      parent = dao.update(parent);
      category = parent.childCategories.stream()
        .filter(child -> Strings.nullOrEqual(child.refId, refID))
        .findAny()
        .orElse(null);
      if (category == null)
        return null;
    }

    if (!Strings.nullOrEqual(id, category.refId)) {
      imp.mappedCategories.put(id, category.refId);
    }
    imp.putHandled(category);
    return category;
  }
}
