package org.openlca.proto.output;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

public class ProductSystemWriter {

  private final WriterConfig config;

  public ProductSystemWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.ProductSystem write(ProductSystem system) {
    var proto = Proto.ProductSystem.newBuilder();
    if (system == null)
      return proto.build();
    Out.map(system, proto);
    Out.dep(config, system.category);
    mapQRef(system, proto);
    mapParameterSets(system, proto);
    return proto.build();
  }

  private void mapQRef(ProductSystem system,
                       Proto.ProductSystem.Builder proto) {
    // ref. process
    if (system.referenceProcess != null) {
      var p = Out.processRefOf(system.referenceProcess);
      proto.setReferenceProcess(p);
    }

    // ref. exchange
    if (system.referenceExchange != null) {
      var e = Proto.ExchangeRef.newBuilder()
        .setInternalId(system.referenceExchange.internalId);
      proto.setReferenceExchange(e);
    }

    // ref. quantity
    if (system.targetFlowPropertyFactor != null) {
      var prop = system.targetFlowPropertyFactor.flowProperty;
      proto.setTargetFlowProperty(Out.refOf(prop));
    }

    // ref. unit
    if (system.targetUnit != null) {
      proto.setTargetUnit(Out.refOf(system.targetUnit));
    }

    // ref. amount
    proto.setTargetAmount(system.targetAmount);
  }

  private void mapParameterSets(ProductSystem system,
                                Proto.ProductSystem.Builder proto) {
    for (var paramSet : system.parameterSets) {
      var protoSet = Proto.ParameterRedefSet.newBuilder();
      protoSet.setName(Strings.orEmpty(paramSet.name));
      protoSet.setDescription(Strings.orEmpty(paramSet.description));
      protoSet.setIsBaseline(paramSet.isBaseline);
      for (var redef : paramSet.parameters) {
        var protoRedef = Proto.ParameterRedef.newBuilder();
        protoRedef.setName(Strings.orEmpty(redef.name));
        protoRedef.setValue(redef.value);
        if (redef.uncertainty != null) {
          var u = Out.uncertaintyOf(redef.uncertainty);
          protoRedef.setUncertainty(u);
        }
        if (redef.contextId != null) {
          var context = redef.contextType == ModelType.PROCESS
            ? config.db.getDescriptor(Process.class, redef.contextId)
            : config.db.getDescriptor(ImpactCategory.class, redef.contextId);
          if (context != null) {
            protoRedef.setContext(Out.refOf(context));
          }
        }
        protoSet.addParameters(protoRedef);
      }
      proto.addParameterSets(protoSet);
    }
  }
}
