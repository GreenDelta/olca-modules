package org.openlca.proto.input;

import java.util.Objects;

import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.proto.Proto;

public class ImpactMethodImport {

  private final ProtoImport imp;

  public ImpactMethodImport(ProtoImport imp) {
    this.imp = imp;
  }

  public ImpactMethod of(String id) {
    if (id == null)
      return null;
    var method = imp.get(ImpactMethod.class, id);

    // check if we are in update mode
    var update = false;
    if (method != null) {
      update = imp.shouldUpdate(method);
      if(!update) {
        return method;
      }
    }

    // check the proto object
    var proto = imp.store.getImpactMethod(id);
    if (proto == null)
      return method;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(method, wrap))
        return method;
    }

    // map the data
    if (method == null) {
      method = new ImpactMethod();
      method.refId = id;
    }
    wrap.mapTo(method, imp);
    map(proto, method);

    // insert it
    var dao = new ImpactMethodDao(imp.db);
    method = update
      ? dao.update(method)
      : dao.insert(method);
    imp.putHandled(method);
    return method;
  }

  private void map(Proto.ImpactMethod proto, ImpactMethod method) {

    for (var protoImp : proto.getImpactCategoriesList()) {
      var impactID = protoImp.getId();
      var impact = new ImpactCategoryImport(imp).of(impactID);
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

  private NwFactor nwFactor(Proto.NwFactor proto, ImpactMethod method) {
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
