package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Version;
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

    // root entity fields
    proto.setType("ProductSystem");
    proto.setId(Strings.orEmpty(system.refId));
    proto.setName(Strings.orEmpty(system.name));
    proto.setDescription(Strings.orEmpty(system.description));
    proto.setVersion(Version.asString(system.version));
    if (system.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(system.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(system.tags)) {
      Arrays.stream(system.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (system.category != null) {
      proto.setCategory(Out.refOf(system.category, config));
    }

    // model specific fields
    mapParameterSets(system, proto);

    if (system.referenceExchange != null) {
      var protoRefEx = Proto.ExchangeRef.newBuilder()
        .setInternalId(system.referenceExchange.internalId);
      proto.setReferenceExchange(protoRefEx);
    }

    // TODO

    return proto.build();
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
