package org.openlca.proto.io.input;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Result;
import org.openlca.core.model.RiskLevel;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.proto.ProtoAllocationType;
import org.openlca.proto.ProtoParameterRedef;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.ProtoRiskLevel;
import org.openlca.proto.ProtoType;
import org.openlca.proto.ProtoUncertainty;
import org.openlca.util.Strings;

class Util {

	private Util() {
	}

	static Uncertainty uncertaintyOf(ProtoUncertainty proto) {
		return switch (proto.getDistributionType()) {
			case NORMAL_DISTRIBUTION -> Uncertainty.normal(
				proto.getMean(), proto.getSd());
			case LOG_NORMAL_DISTRIBUTION -> Uncertainty.logNormal(
				proto.getGeomMean(), proto.getGeomSd());
			case TRIANGLE_DISTRIBUTION -> Uncertainty.triangle(
				proto.getMinimum(), proto.getMode(), proto.getMaximum());
			case UNIFORM_DISTRIBUTION -> Uncertainty.uniform(
				proto.getMinimum(), proto.getMaximum());
			default -> null;
		};
	}

	static AllocationMethod allocationMethodOf(ProtoAllocationType proto) {
		if (proto == null)
			return null;
		return switch (proto) {
			case CAUSAL_ALLOCATION -> AllocationMethod.CAUSAL;
			case ECONOMIC_ALLOCATION -> AllocationMethod.ECONOMIC;
			case PHYSICAL_ALLOCATION -> AllocationMethod.PHYSICAL;
			case USE_DEFAULT_ALLOCATION -> AllocationMethod.USE_DEFAULT;
			default -> AllocationMethod.NONE;
		};
	}

	static Optional<Date> dateOf(String field) {
		if (Strings.nullOrEmpty(field))
			return Optional.empty();
		var date = Json.parseDate(field);
		return Optional.ofNullable(date);
	}

	static RiskLevel riskLevelOf(ProtoRiskLevel proto) {
		return switch (proto) {
			case HIGH_OPPORTUNITY -> RiskLevel.HIGH_OPPORTUNITY;
			case HIGH_RISK -> RiskLevel.HIGH_RISK;
			case LOW_OPPORTUNITY -> RiskLevel.LOW_OPPORTUNITY;
			case LOW_RISK -> RiskLevel.LOW_RISK;
			case MEDIUM_OPPORTUNITY -> RiskLevel.MEDIUM_OPPORTUNITY;
			case MEDIUM_RISK -> RiskLevel.MEDIUM_RISK;
			case NOT_APPLICABLE -> RiskLevel.NOT_APPLICABLE;
			case NO_DATA -> RiskLevel.NO_DATA;
			case NO_OPPORTUNITY -> RiskLevel.NO_OPPORTUNITY;
			case NO_RISK -> RiskLevel.NO_RISK;
			case VERY_HIGH_RISK -> RiskLevel.VERY_HIGH_RISK;
			case VERY_LOW_RISK -> RiskLevel.VERY_LOW_RISK;
			default -> null;
		};
	}

	static List<ParameterRedef> parameterRedefsOf(
		List<ProtoParameterRedef> protos, EntityResolver resolver) {
		var redefs = new ArrayList<ParameterRedef>();
		for (var proto : protos) {
			var p = new ParameterRedef();
			redefs.add(p);
			p.name = proto.getName();
			p.description =proto.getDescription();
			p.value = proto.getValue();
			p.uncertainty = uncertaintyOf(proto.getUncertainty());
			p.isProtected = proto.getIsProtected();

			// parameter context
			if (proto.hasContext()) {
				var protoCxt = proto.getContext();
				if (protoCxt.getType() == ProtoType.Process) {
					var d = resolver.getDescriptor(Process.class, protoCxt.getId());
					if (d != null) {
						p.contextType = ModelType.PROCESS;
						p.contextId = d.id;
					}
				} else if (protoCxt.getType() == ProtoType.ImpactCategory) {
					var d = resolver.getDescriptor(ImpactCategory.class, protoCxt.getId());
					if (d != null) {
						p.contextType = ModelType.IMPACT_CATEGORY;
						p.contextId = d.id;
					}
				}
			}
		}
		return redefs;
	}

	static void mapBase(RootEntity e, ProtoBox<?, ?> proto, EntityResolver resolver) {
		if (proto == null)
			return;
		e.refId = proto.id();
		e.name = proto.name();
		e.description = proto.description();
		e.version = versionOf(proto);
		e.lastChange = lastChangeOf(proto);

		// category
		var path = proto.category();
		if (Strings.notEmpty(path)) {
			var type = ModelType.of(e);
			e.category = resolver.getCategory(type, path);
		}

		// tags
		if (!proto.tags().isEmpty()) {
			e.tags = String.join(",", proto.tags());
		}
	}

	static long versionOf(ProtoBox<?, ?> proto) {
		var s = proto.version();
		return Strings.nullOrEmpty(s)
			? 0
			: Version.fromString(s).getValue();
	}

	static long lastChangeOf(ProtoBox<?, ?> proto) {
		var s = proto.lastChange();
		if (Strings.nullOrEmpty(s))
			return 0;
		var date = Json.parseDate(s);
		return date != null
			? date.getTime()
			: 0;
	}

	static Actor getActor(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(Actor.class, id)
			: null;
	}

	static Currency getCurrency(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(Currency.class, id)
			: null;
	}

	static DQSystem getDQSystem(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(DQSystem.class, id)
			: null;
	}

	static Epd getEpd(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(Epd.class, id)
			: null;
	}

	static FlowProperty getFlowProperty(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(FlowProperty.class, id)
			: null;
	}

	static Flow getFlow(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(Flow.class, id)
			: null;
	}

	static ImpactCategory getImpactCategory(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(ImpactCategory.class, id)
			: null;
	}

	static ImpactMethod getImpactMethod(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(ImpactMethod.class, id)
			: null;
	}

	static Location getLocation(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(Location.class, id)
			: null;
	}

	static Parameter getParameter(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(Parameter.class, id)
			: null;
	}

	static Process getProcess(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(Process.class, id)
			: null;
	}

	static ProductSystem getProductSystem(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(ProductSystem.class, id)
			: null;
	}

	static Project getProject(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(Project.class, id)
			: null;
	}

	static Result getResult(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(Result.class, id)
			: null;
	}

	static SocialIndicator getSocialIndicator(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(SocialIndicator.class, id)
			: null;
	}

	static Source getSource(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(Source.class, id)
			: null;
	}

	static UnitGroup getUnitGroup(EntityResolver resolver, ProtoRef ref) {
		var id = ref.getId();
		return Strings.notEmpty(id)
			? resolver.get(UnitGroup.class, id)
			: null;
	}
}
