package org.openlca.proto.io.output;

import org.openlca.core.model.Parameter;
import org.openlca.proto.ProtoParameter;
import org.openlca.proto.ProtoParameterScope;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class ParameterWriter {

  private final WriterConfig config;

  public ParameterWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoParameter write(Parameter parameter) {
    var proto = ProtoParameter.newBuilder();
    if (parameter == null)
      return proto.build();
    proto.setType(ProtoType.Parameter);
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

  private ProtoParameterScope scopeOf(Parameter param) {
    if (param == null || param.scope == null)
      return ProtoParameterScope.GLOBAL_SCOPE;
    return switch (param.scope) {
      case IMPACT -> ProtoParameterScope.IMPACT_SCOPE;
      case PROCESS -> ProtoParameterScope.PROCESS_SCOPE;
      default -> ProtoParameterScope.GLOBAL_SCOPE;
    };
  }
}
