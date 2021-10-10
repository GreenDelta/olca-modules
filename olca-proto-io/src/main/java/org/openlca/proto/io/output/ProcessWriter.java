package org.openlca.proto.io.output;

import java.util.Objects;
import java.util.function.LongFunction;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.SocialAspect;
import org.openlca.proto.ProtoAllocationFactor;
import org.openlca.proto.ProtoAllocationType;
import org.openlca.proto.ProtoExchange;
import org.openlca.proto.ProtoExchangeRef;
import org.openlca.proto.ProtoProcess;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.ProtoRiskLevel;
import org.openlca.proto.ProtoSocialAspect;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

public class ProcessWriter {

  private final WriterConfig config;

  public ProcessWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoProcess write(Process process) {
    var proto = ProtoProcess.newBuilder();
    if (process == null)
      return proto.build();
    proto.setType(ProtoType.Process);
    Out.map(process, proto);
    Out.dep(config, process.category);

    proto.setProcessType(Out.processTypeOf(process.processType));
    proto.setDefaultAllocationMethod(
      allocationType(process.defaultAllocationMethod));
    proto.setInfrastructureProcess(process.infrastructureProcess);
    if (process.location != null) {
      proto.setLocation(Refs.refOf(process.location));
      Out.dep(config, process.location);
    }
    proto.setLastInternalId(process.lastInternalId);

    // DQ systems
    if (process.dqSystem != null) {
      proto.setDqSystem(Refs.refOf(process.dqSystem));
      Out.dep(config, process.dqSystem);
    }
    proto.setDqEntry(Strings.orEmpty(process.dqEntry));
    if (process.exchangeDqSystem != null) {
      proto.setExchangeDqSystem(
        Refs.refOf(process.exchangeDqSystem));
      Out.dep(config, process.exchangeDqSystem);
    }
    if (process.socialDqSystem != null) {
      proto.setSocialDqSystem(
        Refs.refOf(process.socialDqSystem));
      Out.dep(config, process.socialDqSystem);
    }

    // parameters
    var paramWriter = new ParameterWriter(config);
    for (var param : process.parameters) {
      proto.addParameters(paramWriter.write(param));
    }

    writeExchanges(process, proto);
    writeSocialAspects(process, proto);
    writeAllocationFactors(process, proto);

    return proto.build();
  }

  private ProtoAllocationType allocationType(AllocationMethod m) {
    if (m == null)
      return ProtoAllocationType.UNDEFINED_ALLOCATION_TYPE;
    return switch (m) {
      case CAUSAL -> ProtoAllocationType.CAUSAL_ALLOCATION;
      case ECONOMIC -> ProtoAllocationType.ECONOMIC_ALLOCATION;
      case PHYSICAL -> ProtoAllocationType.PHYSICAL_ALLOCATION;
      case NONE -> ProtoAllocationType.NO_ALLOCATION;
      case USE_DEFAULT -> ProtoAllocationType.USE_DEFAULT_ALLOCATION;
    };
  }

  private void writeExchanges(Process p, ProtoProcess.Builder proto) {
    for (var e : p.exchanges) {
      var pe = ProtoExchange.newBuilder();
      pe.setAvoidedProduct(e.isAvoided);
      pe.setInput(e.isInput);
      if (e.baseUncertainty != null) {
        pe.setBaseUncertainty(e.baseUncertainty);
      }
      pe.setAmount(e.amount);
      pe.setAmountFormula(Strings.orEmpty(e.formula));
      pe.setDqEntry(Strings.orEmpty(e.dqEntry));
      pe.setDescription(Strings.orEmpty(e.description));
      pe.setCostFormula(Strings.orEmpty(e.costFormula));
      if (e.costs != null) {
        pe.setCostValue(e.costs);
      }
      if (e.currency != null) {
        pe.setCurrency(Refs.refOf(e.currency));
        Out.dep(config, e.currency);
      }
      pe.setInternalId(e.internalId);
      if (e.uncertainty != null) {
        pe.setUncertainty(Out.uncertaintyOf(e.uncertainty));
      }

      // default provider
      if (e.defaultProviderId > 0) {
        var provider = new ProcessDao(config.db)
          .getDescriptor(e.defaultProviderId);
        if (provider != null) {
          pe.setDefaultProvider(Refs.refOf(provider));
        }
      }

      // flow references
      if (e.flow != null) {
        pe.setFlow(Refs.refOf(e.flow));
        Out.dep(config, e.flow);
      }
      if (e.flowPropertyFactor != null) {
        var fp = e.flowPropertyFactor.flowProperty;
        if (fp != null) {
          pe.setFlowProperty(Refs.refOf(fp));
        }
      }
      if (e.unit != null) {
        pe.setUnit(Refs.refOf(e.unit));
      }

      if (Objects.equals(e, p.quantitativeReference)) {
        pe.setQuantitativeReference(true);
      }

      proto.addExchanges(pe);
    }
  }

  private void writeSocialAspects(Process p, ProtoProcess.Builder proto) {
    for (var aspect : p.socialAspects) {
      var pa = ProtoSocialAspect.newBuilder();
      if (aspect.indicator != null) {
        pa.setSocialIndicator(Refs.refOf(aspect.indicator));
        Out.dep(config, aspect.indicator);
      }
      pa.setComment(Strings.orEmpty(aspect.comment));
      pa.setQuality(Strings.orEmpty(aspect.quality));
      pa.setRawAmount(Strings.orEmpty(aspect.rawAmount));
      pa.setActivityValue(aspect.activityValue);
      pa.setRiskLevel(riskLevel(aspect));
      if (aspect.source != null) {
        pa.setSource(Refs.refOf(aspect.source));
        Out.dep(config, aspect.source);
      }
      proto.addSocialAspects(pa);
    }
  }

  private ProtoRiskLevel riskLevel(SocialAspect aspect) {
    if (aspect == null || aspect.riskLevel == null)
      return ProtoRiskLevel.UNDEFINED_RISK_LEVEL;
    try {
      return ProtoRiskLevel.valueOf(aspect.riskLevel.name());
    } catch (Exception e) {
      return ProtoRiskLevel.UNDEFINED_RISK_LEVEL;
    }
  }

  private void writeAllocationFactors(Process p, ProtoProcess.Builder proto) {
    LongFunction<ProtoRef> product = flowID -> {
      for (var e : p.exchanges) {
        if (e.flow != null && e.flow.id == flowID) {
          return Refs.refOf(e.flow).build();
        }
      }
      return null;
    };

    for (var f : p.allocationFactors) {
      var pf = ProtoAllocationFactor.newBuilder();
      pf.setAllocationType(allocationType(f.method));
      if (f.method == AllocationMethod.CAUSAL && f.exchange != null) {
        var eref = ProtoExchangeRef.newBuilder();
        eref.setInternalId(f.exchange.internalId);
        pf.setExchange(eref);
      }
      var productRef = product.apply(f.productId);
      if (productRef != null) {
        pf.setProduct(productRef);
      }
      pf.setValue(f.value);
      pf.setFormula(Strings.orEmpty(f.formula));
      proto.addAllocationFactors(pf);
    }
  }

}
