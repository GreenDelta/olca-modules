package org.openlca.sd.interop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/// Eagerly resolves all `EntityRef` references in an `SdModel` against
/// the database. Returns an error if any referenced entity is missing.
class Resolver {

	private final ImpactMethod method;
	private final Map<String, ProductSystem> systems;
	private final Map<String, List<ParameterRedef>> params;

	private Resolver(
			ImpactMethod method,
			Map<String, ProductSystem> systems,
			Map<String, List<ParameterRedef>> params) {
		this.method = method;
		this.systems = systems;
		this.params = params;
	}

	static Res<Resolver> of(SdModel model, IDatabase db) {
		var lca = model.lca();

		// resolve the impact method
		ImpactMethod method = null;
		if (lca.impactMethod() != null) {
			var methodRes = get(lca.impactMethod(), ImpactMethod.class, db);
			if (methodRes.isError())
				return methodRes.wrapError("Failed to load LCIA method");
			method = methodRes.value();
		}

		// resolve system bindings
		var systems = new HashMap<String, ProductSystem>();
		var paramLists = new HashMap<String, List<ParameterRedef>>();
		for (var b : lca.systemBindings()) {
			var ref = b.system();
			if (ref == null) continue;

			var systemRes = get(ref, ProductSystem.class, db);
			if (systemRes.isError())
				return systemRes.wrapError("Failed to load product system");
			systems.put(ref.refId(), systemRes.value());

			// resolve the variable bindings
			var params = new ArrayList<ParameterRedef>();
			for (var vb : b.varBindings()) {
				if (vb.varId() == null || Strings.isBlank(vb.parameter()))
					continue;

				var param = new ParameterRedef();
				param.name = vb.parameter();
				var ctx = vb.context();
				if (ctx != null && ctx.type() != null) {
					var d = db.getDescriptor(ctx.type().getModelClass(), ctx.refId());
					if (d != null) {
						param.contextType = ctx.type();
						param.contextId = d.id;
					}
				}
				params.add(param);
			}
			paramLists.put(ref.refId(), params);
		}

		return Res.ok(new Resolver(method, systems, paramLists));
	}

	private static <T extends RootEntity> Res<T> get(
		EntityRef ref, Class<T> type, IDatabase db
	) {
		try {
			T entity =  db.get(type, ref.refId());
			return entity != null
				? Res.ok(entity)
				: Res.error("Referenced data set not found in database: "	+
				ref.name() + " (" + type.getSimpleName() + ": " + ref.refId() + ")");
		} catch (Exception e) {
			return Res.error("Unexpected error while loading entity", e);
		}
	}

	ImpactMethod impactMethod() {
		return method;
	}

	ProductSystem systemOf(SystemBinding b) {
		return b != null && b.system() != null
			? systems.get(b.system().refId())
			: null;
	}

	List<ParameterRedef> paramsOf(SystemBinding b) {
		return b != null && b.system() != null
			? params.get(b.system().refId())
			: List.of();
	}
}
