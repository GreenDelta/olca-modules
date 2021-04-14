package org.openlca.proto.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.Services;
import org.openlca.proto.input.In;
import org.openlca.proto.output.Refs;
import org.openlca.util.Strings;

final class Descriptors {

  private Descriptors() {
  }

  static Stream<Proto.Ref> get(IDatabase db, Services.DescriptorRequest req) {
    if (db == null || req == null)
      return Stream.empty();
    return new Search(db, req).get()
      .map(d -> Refs.refOf(d).build());
  }

  private static class Search {

    private final IDatabase db;
    private final Services.DescriptorRequest req;
    private final ModelType type;
    private final RootEntityDao<?, ?> dao;

    Search(IDatabase db, Services.DescriptorRequest req) {
      this.db = db;
      this.req = req;
      type = In.modelTypeOf(req.getType());
      dao = type == null || type == ModelType.UNKNOWN
        ? null
        : Daos.root(db, type);
    }

    Stream<? extends Descriptor> get() {

      // get by ID
      var id = req.getId();
      if (Strings.notEmpty(id)) {
        if (dao != null) {
          var d = dao.getDescriptorForRefId(id);
          return d == null
            ? Stream.empty()
            : Stream.of(d);
        }
        return daos()
          .map(dao -> dao.getDescriptorForRefId(id))
          .filter(Objects::nonNull);
      }

      var stream = all();

      // filter by category
      var categoryReq = req.getCategory();
      if (Strings.notEmpty(categoryReq)) {
        var category = category(categoryReq);
        if (category == null)
          return Stream.empty();
        stream = stream.filter(d -> {
          if (!(d instanceof CategorizedDescriptor))
            return false;
          var cd = (CategorizedDescriptor) d;
          return cd.category != null && cd.category == category.id;
        });
      }

      // filter by name
      var name = req.getName();
      if (Strings.nullOrEmpty(name))
        return stream;
      var term = name.trim().toLowerCase();
      return stream.filter(d -> {
        if (d.name == null)
          return false;
        var other = d.name.trim().toLowerCase();
        return other.equals(term);
      });
    }

    private Stream<? extends Descriptor> all() {
      return dao != null
        ? dao.getDescriptors().stream()
        : daos().map(RootEntityDao::getDescriptors)
        .flatMap(Collection::stream);
    }

    private Stream<? extends RootEntityDao<?, ?>> daos() {
      return Arrays.stream(ModelType.values())
        .filter(type -> type.getModelClass() != null)
        .map(type -> Daos.root(db, type))
        .filter(Objects::nonNull);
    }

    private Category category(String idOrPath) {
      if (type == null)
        return null;
      var dao = new CategoryDao(db);
      var category = dao.getForRefId(idOrPath);
      return category != null
        ? category
        : dao.getForPath(type, idOrPath);
    }
  }
}
