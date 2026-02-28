package org.openlca.sd.interop;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.sd.model.EntityRef;
import org.openlca.sd.model.SdModel;
import org.openlca.sd.model.SystemBinding;
import org.openlca.sd.model.VarBinding;

import java.util.HashMap;
import java.util.Map;

class Resolver {

	private final ImpactMethod method;
	private final Map<String, ProductSystem> systems;
	private final Map<String, ParameterRedef> params;

	private Resolver(
		ImpactMethod method,
		Map<String, ProductSystem> systems,
		Map<String, ParameterRedef> params) {
		this.method = method;
		this.systems = systems;
		this.params = params;
	}

	static Res<Resolver> of(SdModel model, IDatabase db) {
		var lca = model.lca();

		// resolve the impact method
		ImpactMethod method = null;
		if (lca.impactMethod() != null) {
			var metRes = get(lca.impactMethod(), ImpactMethod.class, db);
			if (metRes.isError()) {
				return metRes.wrapError("Failed to load LCIA method");
			}
			method = metRes.value();
		}

		// resolve system & parameter bindings
		var systems = new HashMap<String, ProductSystem>();
		var params = new HashMap<String, ParameterRedef>();
		for (var b : lca.systemBindings()) {
			var ref = b.system();
			if (ref == null) continue;
			var sysRes = get(ref, ProductSystem.class, db);
			if (sysRes.isError()) {
				return sysRes.wrapError("Failed to load product system");
			}
			systems.put(ref.refId(), sysRes.value());
			resolveParams(b, db, params);
		}

		return Res.ok(new Resolver(method, systems, params));
	}

	private static <T extends RootEntity> Res<T> get(
		EntityRef ref, Class<T> type, IDatabase db
	) {
		try {
			T entity = db.get(type, ref.refId());
			return entity != null
				? Res.ok(entity)
				: Res.error("Referenced data set not found in database: " +
				ref.name() + " (" + type.getSimpleName() + ": " + ref.refId() + ")");
		} catch (Exception e) {
			return Res.error("Unexpected error while loading entity", e);
		}
	}

	private static void resolveParams(
		SystemBinding sysLink, IDatabase db, Map<String, ParameterRedef> params
	) {
		for (var v : sysLink.varBindings()) {
			if (v.varId() == null || Strings.isBlank(v.parameter())) {
				continue;
			}
			var param = new ParameterRedef();
			param.name = v.parameter();
			resolveParamContext(v.context(), param, db);
			params.put(keyOf(sysLink, v), param);
		}
	}

	private static void resolveParamContext(
		EntityRef ctx, ParameterRedef param, IDatabase db
	) {
		if (ctx == null || ctx.type() == null) return;
		var d = db.getDescriptor(ctx.type().getModelClass(), ctx.refId());
		if (d != null) {
			param.contextType = ctx.type();
			param.contextId = d.id;
		}
	}

	private static String keyOf(SystemBinding sysLink, VarBinding varLink) {
		var key = "";
		if (sysLink != null && sysLink.system() != null) {
			key += norm(sysLink.system().refId());
		}
		if (varLink != null) {
			key += "/" + norm(varLink.parameter());
			if (varLink.context() != null) {
				key += "/" + norm(varLink.context().refId());
			}
		}
		return key;
	}

	private static String norm(String s) {
		return s != null ? s.trim().toLowerCase() : "";
	}


	ImpactMethod impactMethod() {
		return method;
	}

	ProductSystem systemOf(SystemBinding sysLink) {
		return sysLink != null && sysLink.system() != null
			? systems.get(sysLink.system().refId())
			: null;
	}

	/// Returns a copy of the parameter redefinition if it can be resolved,
	/// so that it can be directly used. Returns `null` if it cannot be
	/// resolved.
	ParameterRedef paramOf(SystemBinding sysLink, VarBinding varLink) {
		var param = params.get(keyOf(sysLink, varLink));
		return param != null ? param.copy() : null;
	}
}
