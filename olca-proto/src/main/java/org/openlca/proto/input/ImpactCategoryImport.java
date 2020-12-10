package org.openlca.proto.input;

import java.util.Objects;

import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Parameter;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class ImpactCategoryImport {

  private final ProtoImport imp;

  public ImpactCategoryImport(ProtoImport imp) {
    this.imp = imp;
  }

  public ImpactCategory of(String id) {
    if (id == null)
      return null;
    var impact = imp.get(ImpactCategory.class, id);

    // check if we are in update mode
    var update = false;
    if (impact != null) {
      update = imp.shouldUpdate(impact);
      if(!update) {
        return impact;
      }
    }

    // check the proto object
    var proto = imp.store.getImpactCategory(id);
    if (proto == null)
      return impact;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(impact, wrap))
        return impact;
    }

    // map the data
    if (impact == null) {
      impact = new ImpactCategory();
      impact.refId = id;
    }
    wrap.mapTo(impact, imp);
    map(proto, impact);

    // insert it
    var dao = new ImpactCategoryDao(imp.db);
    impact = update
      ? dao.update(impact)
      : dao.insert(impact);
    imp.putHandled(impact);
    return impact;
  }

  private void map(Proto.ImpactCategory proto, ImpactCategory impact) {
    impact.referenceUnit = proto.getReferenceUnitName();

    // parameters
    for (var protoParam : proto.getParametersList()) {
      var param = new Parameter();
      ProtoWrap.of(protoParam).mapTo(param, imp);
      ParameterImport.map(protoParam, param);
      impact.parameters.add(param);
    }

    // impact factors
    for (var protoFac : proto.getImpactFactorsList()) {
      var factor = new ImpactFactor();
      impact.impactFactors.add(factor);
      factor.value = protoFac.getValue();
      factor.formula = Strings.notEmpty(protoFac.getFormula())
        ? protoFac.getFormula()
        : null;
      factor.uncertainty = In.uncertainty(protoFac.getUncertainty());

      // flow
      var flowID = protoFac.getFlow().getId();
      factor.flow = new FlowImport(imp).of(flowID);
      if (factor.flow == null)
        continue;

      // location
      var locID = protoFac.getLocation().getId();
      if (Strings.notEmpty(locID)) {
        factor.location = new LocationImport(imp).of(locID);
      }

      // set the flow property and unit; if they are not defined
      // we will choose the reference data from the flow

      // flow property
      var propID = protoFac.getFlowProperty().getId();
      if (Strings.nullOrEmpty(propID)) {
        factor.flowPropertyFactor = factor.flow.getReferenceFactor();
      }
      if (Strings.notEmpty(propID)) {
        factor.flowPropertyFactor = factor.flow
          .flowPropertyFactors.stream()
          .filter(f -> f.flowProperty != null
            && Objects.equals(f.flowProperty.refId, propID))
          .findAny()
          .orElse(null);
      }

      // unit
      if (factor.flowPropertyFactor == null)
        continue;
      var prop = factor.flowPropertyFactor.flowProperty;
      if (prop == null || prop.unitGroup == null)
        return;
      var unitID = protoFac.getUnit().getId();
      if (Strings.nullOrEmpty(unitID)) {
        factor.unit = prop.unitGroup.referenceUnit;
      } else {
        factor.unit = prop.unitGroup.units.stream()
          .filter(u -> Objects.equals(u.refId, unitID))
          .findAny()
          .orElse(null);
      }
    }
  }
}
