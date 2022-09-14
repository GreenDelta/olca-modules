package org.openlca.proto.io.output;

import java.util.Objects;
import java.util.function.LongFunction;

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

		Out.map(process, proto);
		proto.setType(ProtoType.Process);

    proto.setProcessType(Out.processTypeOf(process.processType));
    proto.setDefaultAllocationMethod(
      allocationType(process.defaultAllocationMethod));
    proto.setIsInfrastructureProcess(process.infrastructureProcess);
		config.dep(process.location, proto::setLocation);
    // TODO: process documentation
		proto.setLastInternalId(process.lastInternalId);

    // DQ systems
		config.dep(process.dqSystem, proto::setDqSystem);
    proto.setDqEntry(Strings.orEmpty(process.dqEntry));
		config.dep(process.exchangeDqSystem, proto::setExchangeDqSystem);
		config.dep(process.socialDqSystem, proto::setSocialDqSystem);

    // parameters
    var paramWriter = new ParameterWriter();
    for (var param : process.parameters) {
      proto.addParameters(paramWriter.write(param));
    }

    writeExchanges(process, proto);
    writeSocialAspects(process, proto);
    writeAllocationFactors(process, proto);

    return proto.build();
  }

  private void writeExchanges(Process p, ProtoProcess.Builder proto) {
    for (var e : p.exchanges) {
      var pe = ProtoExchange.newBuilder();
      pe.setIsAvoidedProduct(e.isAvoided);
      pe.setIsInput(e.isInput);
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
			config.dep(e.currency, pe::setCurrency);

      pe.setInternalId(e.internalId);
			config.dep(e.location, pe::setLocation);

      if (e.uncertainty != null) {
        pe.setUncertainty(Out.uncertaintyOf(e.uncertainty));
      }

      // default provider
      if (e.defaultProviderId > 0) {
        var provider = config.db()
          .getDescriptor(Process.class, e.defaultProviderId);
        if (provider != null) {
          pe.setDefaultProvider(Refs.refOf(provider));
        }
      }

      // flow references
			config.dep(e.flow, pe::setFlow);
      if (e.flowPropertyFactor != null) {
        var fp = e.flowPropertyFactor.flowProperty;
				config.dep(fp, pe::setFlowProperty);
      }
			config.dep(e.unit, pe::setUnit);

      if (Objects.equals(e, p.quantitativeReference)) {
        pe.setIsQuantitativeReference(true);
      }

      proto.addExchanges(pe);
    }
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

  private void writeSocialAspects(Process p, ProtoProcess.Builder proto) {
    for (var aspect : p.socialAspects) {
      var protoAspect = ProtoSocialAspect.newBuilder();
			config.dep(aspect.indicator, protoAspect::setSocialIndicator);
      protoAspect.setComment(Strings.orEmpty(aspect.comment));
      protoAspect.setQuality(Strings.orEmpty(aspect.quality));
      protoAspect.setRawAmount(Strings.orEmpty(aspect.rawAmount));
      protoAspect.setActivityValue(aspect.activityValue);
      protoAspect.setRiskLevel(riskLevel(aspect));
			config.dep(aspect.source, protoAspect::setSource);
      proto.addSocialAspects(protoAspect);
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
