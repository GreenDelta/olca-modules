package org.openlca.io.olca;

import org.openlca.commons.Strings;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;

final class ParameterTransfer implements EntityTransfer<Parameter> {

	private final TransferConfig conf;

	ParameterTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		var dao = new ParameterDao(conf.source());
		for (var origin : dao.getGlobalParameters()) {
			sync(origin);
		}
	}

	@Override
	public Parameter sync(Parameter origin) {
		if (origin == null) return null;
		var mapped = conf.getMapped(origin);
		if (mapped != null) return mapped;

		if (origin.scope != ParameterScope.GLOBAL)
			return conf.sync(origin, origin::copy);

		// if there is already a global parameter with that name
		// defined in the target database, we return that parameter
		// and do not create a new one
		var existing = new ParameterDao(conf.target())
			.getGlobalParameters()
			.stream()
			.filter(p -> eq(p.name, origin.name))
			.findAny()
			.orElse(null);
		if (existing != null) {
			conf.seq().put(ModelType.PARAMETER, origin.id, existing.id);
			conf.log().skipped(origin);
			return existing;
		}

		return conf.sync(origin, origin::copy);
	}

	private boolean eq(String a, String b) {
		if (a == null && b == null) return true;
		if (a == null || b == null) return false;
		return Strings.equalsIgnoreCase(a.strip(), b.strip());
	}
}
