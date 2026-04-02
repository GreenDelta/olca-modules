package org.openlca.io.olca;

import org.openlca.commons.Strings;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;

final class ParameterTransfer implements EntityTransfer<Parameter> {

	private final TransferContext ctx;

	ParameterTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		var dao = new ParameterDao(ctx.source());
		for (var origin : dao.getGlobalParameters()) {
			sync(origin);
		}
	}

	@Override
	public Parameter sync(Parameter origin) {
		if (origin == null) return null;
		var mapped = ctx.getMapped(origin);
		if (mapped != null) return mapped;

		if (origin.scope != ParameterScope.GLOBAL)
			return ctx.sync(origin, origin::copy);

		// if there is already a global parameter with that name
		// defined in the target database, we return that parameter
		// and do not create a new one
		var existing = new ParameterDao(ctx.target())
			.getGlobalParameters()
			.stream()
			.filter(p -> eq(p.name, origin.name))
			.findAny()
			.orElse(null);
		if (existing != null) {
			ctx.seq().put(ModelType.PARAMETER, origin.id, existing.id);
			ctx.log().skipped(origin);
			return existing;
		}

		return ctx.sync(origin, origin::copy);
	}

	private boolean eq(String a, String b) {
		if (a == null && b == null) return true;
		if (a == null || b == null) return false;
		return Strings.equalsIgnoreCase(a.strip(), b.strip());
	}
}
