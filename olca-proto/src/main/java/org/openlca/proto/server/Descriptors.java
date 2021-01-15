package org.openlca.proto.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.Services;
import org.openlca.proto.input.In;
import org.openlca.proto.output.Out;
import org.openlca.util.Strings;

final class Descriptors {

  private Descriptors() {
  }

  static Stream<Proto.Ref> get(IDatabase db, Services.DescriptorRequest req) {
    return db == null || req == null
      ? Stream.empty()
      : byName(byID(daosOf(db, req), req), req).map(d -> Out.refOf(d).build());
  }

  private static Stream<? extends RootEntityDao<?, ?>> daosOf(
    IDatabase db, Services.DescriptorRequest req) {
    var modelType = In.modelTypeOf(req.getType());
    if (modelType != null) {
      var dao = Daos.root(db, modelType);
      return dao == null
        ? Stream.empty()
        : Stream.of(dao);
    }
    return Arrays.stream(ModelType.values())
      .filter(type -> type.getModelClass() != null)
      .map(type -> Daos.root(db, type))
      .filter(Objects::nonNull);
  }

  private static Stream<? extends Descriptor> byID(
    Stream<? extends RootEntityDao<?, ? extends Descriptor>> daos,
    Services.DescriptorRequest req) {
    var id = req.getId();
    if (Strings.notEmpty(id)) {
      return daos.map(dao -> dao.getDescriptorForRefId(id))
        .filter(Objects::nonNull);
    }
    return daos.map(RootEntityDao::getDescriptors)
      .flatMap(Collection::stream);
  }

  private static Stream<? extends Descriptor> byName(
    Stream<? extends Descriptor> descriptors,
    Services.DescriptorRequest req) {
    var name = req.getName();
    if (name.isBlank())
      return descriptors;
    var term = name.trim().toLowerCase();
    Predicate<String> match = other ->
      other != null && other.trim().toLowerCase().equals(term);
    return descriptors.filter(d -> match.test(d.name));
  }
}
