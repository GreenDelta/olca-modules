package org.openlca.proto.input;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.proto.Proto;

public class ParameterImport {

  private final ProtoImport imp;

  public ParameterImport(ProtoImport imp) {
    this.imp = imp;
  }

  public Parameter of(String id) {
    if (id == null)
      return null;
    var param = imp.get(Parameter.class, id);

    // check if we are in update mode
    var update = false;
    if (param != null) {
      update = imp.shouldUpdate(param);
      if(!update) {
        return param;
      }
    }

    // check the proto object
    var proto = imp.store.getParameter(id);
    if (proto == null)
      return param;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(param, wrap))
        return param;
    }

    // map the data
    if (param == null) {
      param = new Parameter();
      param.refId = id;
    }
    wrap.mapTo(param, imp);
    map(proto, param);

    // insert it
    var dao = new ParameterDao(imp.db);
    param = update
      ? dao.update(param)
      : dao.insert(param);
    imp.putHandled(param);
    return param;
  }

  static void map(Proto.Parameter proto, Parameter param) {
    param.scope = scopeOf(proto);
    param.isInputParameter = proto.getInputParameter();
    param.value = proto.getValue();
    param.formula = proto.getFormula();
    param.uncertainty = In.uncertainty(proto.getUncertainty());
  }

  static ParameterScope scopeOf(Proto.Parameter proto) {
    switch (proto.getParameterScope()) {
      case PROCESS_SCOPE:
        return ParameterScope.PROCESS;
      case IMPACT_SCOPE:
        return ParameterScope.IMPACT;
      default:
        return ParameterScope.GLOBAL;
    }
  }
}
