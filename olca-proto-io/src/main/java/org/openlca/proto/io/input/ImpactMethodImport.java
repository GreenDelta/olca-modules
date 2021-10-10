package org.openlca.proto.io.input;

import java.util.Objects;

import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.proto.ProtoImpactMethod;
import org.openlca.proto.ProtoNwFactor;

class ImpactMethodImport implements Import<ImpactMethod> {

  private final ProtoImport imp;

  ImpactMethodImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<ImpactMethod> of(String id) {
    var method = imp.get(ImpactMethod.class, id);

    // check if we are in update mode
    var update = false;
    if (method != null) {
      update = imp.shouldUpdate(method);
      if(!update) {
        return ImportStatus.skipped(method);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getImpactMethod(id);
    if (proto == null)
      return method != null
        ? ImportStatus.skipped(method)
        : ImportStatus.error("Could not resolve ImpactMethod " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(method, wrap))
        return ImportStatus.skipped(method);
    }

    // map the data
    if (method == null) {
      method = new ImpactMethod();
      method.refId = id;
    }
    wrap.mapTo(method, imp);
    map(proto, method);

    // insert or update it
    var dao = new ImpactMethodDao(imp.db);
    method = update
      ? dao.update(method)
      : dao.insert(method);
    imp.putHandled(method);
    return update
      ? ImportStatus.updated(method)
      : ImportStatus.created(method);
  }

  private void map(ProtoImpactMethod proto, ImpactMethod method) {

    for (var protoImp : proto.getImpactCategoriesList()) {
      var impactID = protoImp.getId();
      var impact = new ImpactCategoryImport(imp)
        .of(impactID)
        .model();
      if (impact != null) {
        method.impactCategories.add(impact);
      }
    }

    for (var protoNw : proto.getNwSetsList()) {
      var nw = new NwSet();
      method.nwSets.add(nw);
      nw.refId = protoNw.getId();
      nw.name = protoNw.getName();
      nw.description = protoNw.getDescription();
      nw.weightedScoreUnit = protoNw.getWeightedScoreUnit();
      protoNw.getFactorsList()
        .stream()
        .map(protoFactor -> nwFactor(protoFactor, method))
        .forEach(nw.factors::add);
    }
  }

  private NwFactor nwFactor(ProtoNwFactor proto, ImpactMethod method) {
    var f = new NwFactor();
    var impactID = proto.getImpactCategory().getId();
    f.impactCategory = method.impactCategories.stream()
      .filter(i -> Objects.equals(i.refId, impactID))
      .findAny()
      .orElse(null);
    f.normalisationFactor = proto.getNormalisationFactor() == 0
      ? null
      : proto.getNormalisationFactor();
    f.weightingFactor = proto.getWeightingFactor() == 0
      ? null
      : proto.getWeightingFactor();
    return f;
  }
}
