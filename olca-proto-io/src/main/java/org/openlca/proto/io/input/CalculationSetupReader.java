package org.openlca.proto.io.input;

import java.util.ArrayList;
import java.util.Objects;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.CalculationTarget;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.proto.grpc.ProtoCalculationSetup;
import org.openlca.util.Strings;

public final class CalculationSetupReader {

  private final EntityResolver resolver;

  public CalculationSetupReader(EntityResolver resolver) {
    this.resolver = Objects.requireNonNull(resolver);
  }

  public static CalculationSetup read(
    EntityResolver resolver, ProtoCalculationSetup proto) {
    return new CalculationSetupReader(resolver).read(proto);
  }

  public CalculationSetup read(ProtoCalculationSetup proto) {
    if (proto == null)
      return null;
    var target = targetOf(proto);
    if (target == null)
      return null;

    var setup = CalculationSetup.of(target);
    setQuantity(proto, setup);
    setImpactMethod(proto, setup);
    setParameters(proto, setup);

    // other settings
    setup.withAllocation(Util.allocationMethodOf(proto.getAllocation()))
      .withCosts(proto.getWithCosts())
      .withRegionalization(proto.getWithRegionalization());

    return setup;
  }

  private CalculationTarget targetOf(ProtoCalculationSetup proto) {
    if (proto.hasProductSystem()) {
      var refId = proto.getProductSystem().getId();
      return resolver.get(ProductSystem.class, refId);
    }
    if (proto.hasProcess()) {
      var refId = proto.getProcess().getId();
      return resolver.get(Process.class, refId);
    }
    return null;
  }

  private void setQuantity(
    ProtoCalculationSetup proto, CalculationSetup setup) {

    // amount
    if (proto.getAmount() != 0) {
      setup.withAmount(proto.getAmount());
    }

    var qref = setup.target().quantitativeReference();
    if (qref == null)
      return;

    // flow property
    var propId = proto.getFlowProperty().getId();
    if (Strings.isNotBlank(propId) && qref.flow != null) {
      qref.flow.flowPropertyFactors.stream()
				.filter(f -> Objects.equals(propId, f.flowProperty.refId))
        .findAny()
        .ifPresent(setup::withFlowPropertyFactor);
    }

    // unit
    var unitId = proto.getUnit().getId();
    var propFac = setup.flowPropertyFactor();
    if (Strings.isNotBlank(unitId)
        && propFac != null
        && propFac.flowProperty != null
        && propFac.flowProperty.unitGroup != null) {
      var group = propFac.flowProperty.unitGroup;
      group.units.stream()
				.filter(u -> Objects.equals(unitId, u.refId))
        .findAny()
        .ifPresent(setup::withUnit);
    }
  }

  private void setImpactMethod(
    ProtoCalculationSetup proto, CalculationSetup setup) {

    // impact method
    var methodId = proto.getImpactMethod().getId();
    if (Strings.isBlank(methodId))
      return;
    var method = resolver.get(ImpactMethod.class, methodId);
    if (method == null)
      return;
    setup.withImpactMethod(method);

    // nw-set
    var nwId = proto.getNwSet().getId();
    if (Strings.isBlank(nwId))
      return;
    for (var nwSet : method.nwSets) {
      if (nwId.equals(nwSet.refId)) {
        setup.withNwSet(nwSet);
        break;
      }
    }
  }

  private void setParameters(
    ProtoCalculationSetup proto, CalculationSetup setup) {

    var n = proto.getParametersCount();
    if (n == 0)
      return;

    var redefs = new ArrayList<ParameterRedef>();
    for (int i = 0; i < n; i++) {
      var protoParam = proto.getParameters(i);
      var redef = ParameterRedefReader.read(
        resolver, protoParam);
      if (redef != null) {
        redefs.add(redef);
      }
    }
    if (!redefs.isEmpty()) {
      setup.withParameters(redefs);
    }
  }
}
