package org.openlca.proto.io.input;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.proto.ProtoParameter;

class ParameterImport implements Import<Parameter> {

  private final ProtoImport imp;

  ParameterImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<Parameter> of(String id) {
    var param = imp.get(Parameter.class, id);

    // check if we are in update mode
    var update = false;
    if (param != null) {
      update = imp.shouldUpdate(param);
      if(!update) {
        return ImportStatus.skipped(param);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getParameter(id);
    if (proto == null)
      return param != null
        ? ImportStatus.skipped(param)
        : ImportStatus.error("Could not resolve Parameter " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(param, wrap))
        return ImportStatus.skipped(param);
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
    return update
      ? ImportStatus.updated(param)
      : ImportStatus.created(param);
  }

  static void map(ProtoParameter proto, Parameter param) {
    param.scope = scopeOf(proto);
    param.isInputParameter = proto.getInputParameter();
    param.value = proto.getValue();
    param.formula = proto.getFormula();
    param.uncertainty = In.uncertainty(proto.getUncertainty());
  }

  static ParameterScope scopeOf(ProtoParameter proto) {
    return switch (proto.getParameterScope()) {
      case PROCESS_SCOPE -> ParameterScope.PROCESS;
      case IMPACT_SCOPE -> ParameterScope.IMPACT;
      default -> ParameterScope.GLOBAL;
    };
  }
}
