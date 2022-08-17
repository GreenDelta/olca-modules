package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Actor;
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
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.proto.ProtoRef;
import org.openlca.util.Strings;

class Util {

	private Util() {
	}

	static void mapBase(RootEntity e, ProtoWrap proto, EntityResolver resolver) {
		if (proto == null)
			return;
		e.refId = proto.id();
		e.name = proto.name();
		e.description = proto.description();
		e.version = parseVersion(proto);
		e.lastChange = parseLastChange(proto);

		// category
		var path = proto.category();
		if (Strings.notEmpty(path)) {
			var type = ModelType.of(e);
			e.category = resolver.getCategory(type, path);
		}

		// tags
		if(!proto.tags().isEmpty()) {
			e.tags = String.join(",", proto.tags());
		}
	}

	private static long parseVersion(ProtoWrap proto) {
		var s = proto.version();
		return Strings.nullOrEmpty(s)
			? 0
			: Version.fromString(s).getValue();
	}

	private static long parseLastChange(ProtoWrap proto) {
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
