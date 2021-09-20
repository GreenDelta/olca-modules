package org.openlca.proto.io.output;

import org.openlca.core.model.Parameter;
import org.openlca.proto.EntityType;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class ParameterWriter {

  private final WriterConfig config;

  public ParameterWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.Parameter write(Parameter parameter) {
    var proto = Proto.Parameter.newBuilder();
    if (parameter == null)
      return proto.build();
    proto.setEntityType(EntityType.Parameter);
    Out.map(parameter, proto);
    Out.dep(config, parameter.category);

    proto.setFormula(Strings.orEmpty(parameter.formula));
    proto.setInputParameter(parameter.isInputParameter);
    if (parameter.uncertainty != null) {
      proto.setUncertainty(
        Out.uncertaintyOf(parameter.uncertainty));
    }
    proto.setValue(parameter.value);
    proto.setParameterScope(scopeOf(parameter));

    return proto.build();
  }

  private Proto.ParameterScope scopeOf(Parameter param) {
    if (param == null || param.scope == null)
      return Proto.ParameterScope.GLOBAL_SCOPE;
    switch (param.scope) {
      case IMPACT:
        return Proto.ParameterScope.IMPACT_SCOPE;
      case PROCESS:
        return Proto.ParameterScope.PROCESS_SCOPE;
      default:
        return Proto.ParameterScope.GLOBAL_SCOPE;
    }
  }
}
