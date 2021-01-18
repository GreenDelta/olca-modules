package org.openlca.proto.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.Services;
import org.openlca.proto.input.In;
import org.openlca.proto.output.Out;
import org.openlca.util.CategoryPathBuilder;
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

  public static Decorator decorator(IDatabase db) {
    return new Decorator(db);
  }

  static class Decorator {

    private final IDatabase db;
    private CategoryPathBuilder categories;
    private TLongObjectHashMap<String> flowUnits;
    private TLongObjectHashMap<String> locationCodes;

    private Decorator(IDatabase db) {
      this.db = db;
    }

    Proto.Ref.Builder of(Descriptor d) {
      var proto = Out.refOf(d);

      category(d, proto);

      if (d instanceof FlowDescriptor) {
        var fd = (FlowDescriptor) d;
        proto.setRefUnit(flowUnit(fd.refFlowPropertyId));
        if (fd.location != null) {
          proto.setLocation(location(fd.location));
        }
      } else if (d instanceof ProcessDescriptor) {
        var pd = (ProcessDescriptor) d;
        if (pd.location != null) {
          proto.setLocation(location(pd.location));
        }
      } else if (d instanceof ImpactDescriptor) {
        var id = (ImpactDescriptor) d;
        proto.setRefUnit(Strings.orEmpty(id.referenceUnit));
      }

      return proto;
    }

    private void category(Descriptor d, Proto.Ref.Builder proto) {
      if (!(d instanceof CategorizedDescriptor))
        return;
      var cd = (CategorizedDescriptor) d;
      if (cd.category == null)
        return;
      if (categories == null) {
        categories = new CategoryPathBuilder(db);
      }
      var path = categories.list(cd.category);
      if (path != null && !path.isEmpty()) {
        proto.addAllCategoryPath(path);
      }
    }

    private String flowUnit(long flowPropID) {
      if (flowUnits == null) {
        flowUnits = new TLongObjectHashMap<>();
        var query = "select fp.id, u.name" +
          "  from tbl_flow_properties fp" +
          "  inner join tbl_unit_groups ug" +
          "  on fp.f_unit_group = ug.id" +
          "  inner join tbl_units u" +
          "  on ug.f_reference_unit = u.id";
        NativeSql.on(db).query(query, r -> {
          long propID = r.getLong(1);
          var unit = r.getString(2);
          flowUnits.put(propID, unit);
          return true;
        });
      }
      return Strings.orEmpty(flowUnits.get(flowPropID));
    }

    private String location(long locationID) {
      if (locationCodes == null) {
        locationCodes = new TLongObjectHashMap<>();
        var query = "select id, code, name from tbl_locations";
        NativeSql.on(db).query(query, r -> {
          var locID = r.getLong(1);
          var code = r.getString(2);
          if (Strings.nullOrEmpty(code)) {
            code = r.getString(3);
          }
          if (Strings.notEmpty(code)) {
            locationCodes.put(locID, code);
          }
          return true;
        });
      }
      return Strings.orEmpty(locationCodes.get(locationID));
    }
  }
}
